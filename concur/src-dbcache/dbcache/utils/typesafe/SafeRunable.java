package dbcache.utils.typesafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的SafeRunable
 * 
 * @author Jake
 *
 */
public class SafeRunable implements Runnable {
	
	AtomicReference<SafeRunable> next = new AtomicReference<SafeRunable>(null);
	
	private SafeType safeType;
	
	private SafeActor safeActor;
	
	protected SafeRunable(SafeType safeType, SafeActor safeActor) {
		this.safeType = safeType;
		this.safeActor = safeActor;
	}
	
	
	@Override
	public void run() {
		safeType.currentThread = Thread.currentThread();// 设置当前执行线程
		
		// 迭代执行SafeActor
		if (safeActor.roll()) {
			try {
				
				safeActor.run();
				
				// afterExecute() TODO
				if (safeActor.afterExecuteArgs != null) {
					safeActor.afterExecute(safeActor.afterExecuteArgs);
				}
				
			} catch (Exception e) {
				safeActor.onException(e);
			}
		}
		
		safeType.currentThread = null;// 取消当前执行线程
		// 执行下一个任务
		this.runNext();
	}

	/**
	 * 执行
	 */
	public void execute() {
		// 已经在执行状态内
		if (safeType.currentThread == Thread.currentThread()) {
			this.run();
			return;
		}
		
		// messages from the same client are handled orderly
		AtomicReference<SafeRunable> lastRef = safeType.head;

		if (lastRef.get() == null && lastRef.compareAndSet(null, this)) { // No previous job
			this.run();
		} else {
			// CAS loop
			for (; ; ) {

				SafeRunable last = lastRef.get();

				AtomicReference<SafeRunable> nextRef = last.next;

				SafeRunable next = nextRef.get();
				if (next != null) {
					if (next == last && lastRef.compareAndSet(last, this)) {
						// previous message is handled, order is
						// guaranteed.
						this.run();
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
	
	
	/**
	 * 执行下一个任务
	 */
	protected void runNext() {
		if (!next.compareAndSet(null, this)) { // has more job to run
			next.get().run();
		}
	}
	
}
