package utils.typesafe.extended;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的SafeRunable
 * 
 * @author Jake
 *
 */
public class MultiSafeRunable implements Runnable {

	AtomicReference<MultiSafeRunable> next = new AtomicReference<MultiSafeRunable>(null);

	private MultiSafeType safeType;

	private MultiSafeActor safeActor;

	private ExecutorService executorService;


	protected MultiSafeRunable(MultiSafeType safeType, MultiSafeActor safeActor, ExecutorService executorService) {
		this.safeType = safeType;
		this.safeActor = safeActor;
		this.executorService = executorService;
	}
	
	
	@Override
	public void run() {
		safeExecute();
	}

	/**
	 * safe run
	 * @return 是否执行了当前任务
	 */
	private void safeExecute() {
		// 未到达最后一个SafeType
		if (safeActor.incrementAndGet() < safeActor.getCount()) {
			return;
		}

		try {
			safeActor.run();
		} catch (Exception e) {
			safeActor.onException(e);
			System.out.println(e);
		} finally {
			// 执行下一个任务
			this.executeNext();
		}
	}

	/**
	 * 执行
	 */
	public void execute() {

		// messages from the same client are handled orderly
		AtomicReference<MultiSafeRunable> lastRef = safeType.head;

		MultiSafeRunable last = lastRef.get();
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
	 * 执行下一个任务
	 */
	protected void executeNext() {

		// 派发其他依赖实体线程
		if (this.safeActor.getCount() > 1) {
			this.safeActor.dispathNext(this);
		}

		runNext();
	}

	/**
	 * run 下一个runnable
	 */
	public void runNext() {
		if (!this.next.compareAndSet(null, this)) { // has more job to run
			this.next.get().safeExecute();
		}
	}


	// 提交runNext到线程池
	public void submitRunNext() {
		submitRunnable(new Runnable() {
			@Override
			public void run() {
				runNext();
			}
		});
	}


	// 提交任务
	private void submitRunnable(Runnable safeRunable) {
		executorService.execute(safeRunable);
	}


	@Override
	public String toString() {
		return safeType.toString();
	}

}
