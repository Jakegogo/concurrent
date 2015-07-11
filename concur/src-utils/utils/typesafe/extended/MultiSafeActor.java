package utils.typesafe.extended;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的Actor
 * <br/>支持单个对象锁的顺序执行,可并发提交。与提交线程的关系是同步或异步
 * <br/>序列执行子类型
 * <br/>需要覆盖run方法, 方法内可以调用super.afterExecute(Object[])执行结束后会进行回调
 * @author Jake
 *
 */
public abstract class MultiSafeActor implements AbstractMultiSafeActor, Runnable {

	private MultiSafeType[] safeTypes;
	private MultiSafeRunable[] safeRunables;

	private int count;

	// 计数器
	private AtomicInteger curCount = new AtomicInteger(0);

	private int hash;

	private static AtomicInteger hashGen = new AtomicInteger(0);

	AtomicReference<MultiSafeActor> next = new AtomicReference<MultiSafeActor>(null);

	public MultiSafeActor(ExecutorService executorService, MultiSafeType... safeTypes) {
		if (safeTypes == null || safeTypes.length == 0) {
			throw new IllegalArgumentException("safeTypes must more than one arguments");
		}
		
		this.safeTypes = safeTypes;
		this.count = safeTypes.length;

		initSafeRunnables(safeTypes, executorService);
	}


	// 初始化MultiSafeRunable
	private void initSafeRunnables(MultiSafeType[] safeTypes, ExecutorService executorService) {
		MultiSafeRunable[] safeRunables = new MultiSafeRunable[safeTypes.length];
		for (int i = 0;i < safeTypes.length;i++) {
			safeRunables[i] = new MultiSafeRunable(safeTypes[i], this, executorService);
		}
		this.safeRunables = safeRunables;
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
		AtomicReference<AbstractMultiSafeActor> lastRef = runnable.head;

		if (lastRef.get() == null && lastRef.compareAndSet(null, this)) { // No previous job
			runnable.runQueue();
		} else {
			// CAS loop
			for (; ; ) {

				MultiSafeActor last = (MultiSafeActor) lastRef.get();

				AtomicReference<MultiSafeActor> nextRef = last.next;
				MultiSafeActor next = nextRef.get();

				if (next != null) {
					if (next == last && lastRef.compareAndSet(last, this)) {
						// previous message is handled, order is
						// guaranteed.
						runnable.runQueue();
						return;
					}
				} else if (nextRef.compareAndSet(null, this)) {
					lastRef.compareAndSet(last, this);// fail is OK
					// successfully append to previous task
					return;
				}
			}
		}

	}


	// 执行提交任务
	protected void runQueue() {
		hash = hashGen.incrementAndGet();
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
//		System.out.println("dispatch " + this);
		for (final MultiSafeRunable safeRunable : safeRunables) {
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

	public int getCurCount() {
		return this.curCount.get();
	}


	/**
	 * 执行异常回调()
	 * @param t Throwable
	 */
	public void onException(Throwable t) {}


	@Override
	public String toString() {
		return Arrays.toString(safeTypes) + ", count=" + this.curCount + ", hash=" + this.hashCode();
	}

	@Override
	public int hashCode() {
		return this.hash;
	}
}
