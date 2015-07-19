package utils.typesafe.extended;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的Actor
 * <br/>支持多个对象操作的顺序执行,可并发提交。与提交线程的关系是同步或异步
 * <br/>序列执行子类型
 * <br/>需要覆盖run方法, 方法内可以调用super.afterExecute(Object[])执行结束后会进行回调
 * <br/>不会因为死锁而阻塞线程,但嵌套死锁会导致后续任务不执行(汗！结果相对于死锁了)
 * <br/>建议业务逻辑层不创建和执行MultiSafeActor,由托举容器进行创建和执行
 * @author Jake
 */
public abstract class MultiSafeActor implements Runnable {


	// 上一次执行的Actor(用于顺序提交任务到线程池)
	private static final AtomicReference<MultiSafeActor> head = new AtomicReference<MultiSafeActor>();
	// next MultiSafeActor(用于顺序提交任务到线程池)
	private AtomicReference<MultiSafeActor> next = new AtomicReference<MultiSafeActor>(null);


	// Actor的线程安全对象的总数量(== safeTypes.length)
	private int count;
	// 计数器,当前的已经执行完成的线程安全操作(<= count)
	private AtomicInteger curCount = new AtomicInteger(0);

	// 执行的线程池
	private ExecutorService executorService;


	// actor对应的线程安全类型对象
	private List<MultiSafeType> safeTypes;
	// 每个线程安全对象需执行的操作
	private List<MultiSafeRunable> safeRunables;


	/**
	 * 构造方法
	 * @param executorService 线程池
	 * @param promiseable 线程安全的操作组
	 */
	public MultiSafeActor(ExecutorService executorService, Promiseable<?> promiseable) {
		if (promiseable == null) {
			throw new IllegalArgumentException("promiseable cannot be null");
		}

		this.safeTypes = new ArrayList<MultiSafeType>();
		this.safeRunables = new ArrayList<MultiSafeRunable>();
		this.executorService = executorService;

		this.when(promiseable);
	}

	/**
	 * 构造方法
	 * @param executorService 线程池
	 * @param safeTypes 线程安全的类型
	 */
	public MultiSafeActor(ExecutorService executorService, MultiSafeType... safeTypes) {
		if (safeTypes == null || safeTypes.length == 0) {
			throw new IllegalArgumentException("safeTypes must more than one arguments");
		}

		List<MultiSafeType> safeTypesList = new ArrayList<MultiSafeType>();
		for (MultiSafeType safeType : safeTypes) {
			safeTypesList.add(safeType);
		}
		this.safeTypes = safeTypesList;

		this.count = safeTypes.length;
		this.executorService = executorService;

		initSafeRunnables(safeTypes, executorService);
	}


	// 初始化MultiSafeRunable
	private void initSafeRunnables(MultiSafeType[] safeTypes, ExecutorService executorService) {
		List<MultiSafeRunable> safeRunables = new ArrayList<MultiSafeRunable>();
		for (int i = 0;i < safeTypes.length;i++) {
			safeRunables.add(new MultiSafeRunable(safeTypes[i], this, executorService));
		}
		this.safeRunables = safeRunables;
	}


	/**
	 * 当获取所需要的关联操作后,when代表将子操作纳入本操作组成新的原子性操作
	 * <br/> 需在start()之前执行
	 * @param promiseables Promiseable[] 线程安全的操作组
	 * @return
	 */
	public MultiSafeActor when(Promiseable<?>... promiseables) {
		if (promiseables == null || promiseables.length == 0) {
			throw new IllegalArgumentException("promiseables must more than one arguments");
		}

		for (int i = 0;i < promiseables.length;i++) {
			Promiseable<?> promiseable = promiseables[i];
			for (MultiSafeType safeType : promiseable.promiseTypes()) {
				safeTypes.add(safeType);
				safeRunables.add(new MultiSafeRunable(safeType, this, executorService));
			}
		}

		this.count += promiseables.length;
		return this;
	}


	/**
     * 开始执行Actor
     */
    public void start() {
		addToQueue(this);
    }


	// 追加提交任务
	protected void addToQueue(MultiSafeActor runnable) {

		// messages from the same client are handled orderly
		AtomicReference<MultiSafeActor> lastRef = head;

		if (lastRef.get() == null && lastRef.compareAndSet(null, this)) { // No previous job
			runnable.runQueue();
		} else {
			// CAS loop
			int casLoop = 1;
			for (; ; ) {

				if (casAppend(runnable, lastRef))
					return;

				if (casLoop ++ > 3) {
					break;
				}
			}

			synchronized (head) {
				for (; ; ) {
					if (casAppend(runnable, lastRef))
						return;
				}
			}

		}

	}

	// 追加到末尾
	private boolean casAppend(MultiSafeActor runnable, AtomicReference<MultiSafeActor> lastRef) {
		MultiSafeActor last = lastRef.get();

		AtomicReference<MultiSafeActor> nextRef = last.next;
		MultiSafeActor next = nextRef.get();

		if (next != null) {
            if (next == last && lastRef.compareAndSet(last, this)) {
                // previous message is handled, order is
                // guaranteed.
                runnable.runQueue();
				return true;
            }
        } else if (nextRef.compareAndSet(null, this)) {
            lastRef.compareAndSet(last, this);// fail is OK
            // successfully append to previous task
			return true;
        }
		return false;
	}


	// 执行提交任务
	protected void runQueue() {
		// 执行SafeRunable序列
		for (final MultiSafeRunable safeRunable : safeRunables) {
			safeRunable.execute();
		}

		this.runNextElement();
	}


	// 执行下一个提交任务
	protected void runNextElement() {
		if (!next.compareAndSet(null, this)) { // has more job to run
			next.get().runQueue();
		}
	}


	/**
	 * 派发依赖实体线程
	 * @param exclude 对后一个依赖执行的Runnable
 	 */
	public void dispathNext(MultiSafeRunable exclude) {
		for (MultiSafeRunable safeRunable : safeRunables) {
			if (safeRunable != exclude) {
				safeRunable.submitRunNext();
			}
		}
	}


	public int incrementAndGet() {
		return this.curCount.incrementAndGet();
	}

	public int getCount() {
		return count;
	}

	/**
	 * 执行异常回调()
	 * @param t Throwable
	 */
	public void onException(Throwable t) {}


	@Override
	public String toString() {
		return safeTypes + ", count=" + this.curCount + ", hash=" + this.hashCode();
	}

}
