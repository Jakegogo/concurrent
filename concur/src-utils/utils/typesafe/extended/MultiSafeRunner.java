package utils.typesafe.extended;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的SafeRunable
 * @author Jake
 */
public class MultiSafeRunner implements Runnable {

	// 下一个将要执行的MultiSafeRunable
	AtomicReference<MultiSafeRunner> next = new AtomicReference<MultiSafeRunner>(null);

	// 执行线程池
	private ExecutorService executorService;


	/** 关联的线程安全类型对象 */
	private MultiSafeType safeType;

	/** 对应原子性操作 */
	private MultiSafeActor safeActor;


	protected MultiSafeRunner(MultiSafeType safeType, MultiSafeActor safeActor, ExecutorService executorService) {
		this.safeType = safeType;
		this.safeActor = safeActor;
		this.executorService = executorService;
	}
	
	
	@Override
	public void run() {
		innerRun();
	}

	/**
	 * safe run
	 * @return 是否执行了当前任务
	 */
	private void innerRun() {
		// 未到达最后一个SafeType
		if (safeActor.incrementAndGet() < safeActor.getCount()) {
			return;
		}

		MultiSafeRunner next = this;
		do {
			try {
				next.safeActor.run();
			} catch (Exception e) {
				next.safeActor.onException(e);
			}
			// 派发其他依赖实体线程
			if (next.safeActor.getCount() > 1) {
				next.safeActor.recoverNext(next);
			}
		} while ((next = next.next()) != null);// 获取下一个任务

	}

	/**
	 * 执行
	 */
	public void execute() {

		// messages from the same client are handled orderly
		AtomicReference<MultiSafeRunner> lastRef = safeType.head;

		MultiSafeRunner last = lastRef.get();
		lastRef.set(this);

		if (last == null) { // No previous job
			submitRunnable(this);
		} else {
			if (last.next.compareAndSet(null, this)) {
				// successfully append to previous task
			} else {
				// previous message is handled, order is guaranteed.
				submitRunnable(this);
			}
		}

	}


	/**
	 * 获取下一个任务
	 */
	protected MultiSafeRunner next() {
		if (!this.next.compareAndSet(null, this)) { // has more job to run
			return next.get();
		}
		return null;
	}


	// 提交runNext到线程池
	public void submitNext() {
		submitRunnable(new Runnable() {
			@Override
			public void run() {
				runNext();
			}
		});
	}

	/**
	 * run 下一个runnable (忽略当前Runner)
	 */
	public void runNext() {
		if (!this.next.compareAndSet(null, this)) { // has more job to run
			this.next.get().run();
		}
	}

	// 提交任务
	private void submitRunnable(Runnable safeRunable) {
		try {
			executorService.execute(safeRunable);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public MultiSafeActor getSafeActor() {
		return safeActor;
	}

	@Override
	public String toString() {
		return safeType.toString();
	}

}
