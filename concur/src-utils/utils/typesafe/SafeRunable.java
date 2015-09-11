package utils.typesafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的SafeRunable
 * @author Jake
 */
public class SafeRunable implements Runnable {
	
	static AtomicInteger gen = new AtomicInteger();
	
	int id = gen.incrementAndGet();
	
	AtomicReference<SafeRunable> next = new AtomicReference<SafeRunable>();

	SafeType safeType;
	
	private SafeActor safeActor;
	
	protected SafeRunable(SafeType safeType, SafeActor safeActor) {
		this.safeType = safeType;
		this.safeActor = safeActor;
	}
	
	
	@Override
	public void run() {
		try {
			safeActor.run();
		} catch (Exception e) {
			safeActor.onException(e);
		} finally {
			// 执行下一个任务
			safeActor.runNext();
		}
	}

	/**
	 * 执行
	 */
	public void execute() {

		// messages from the same client are handled orderly
		if (safeType.casTail(null, this)) { // No previous job
//			System.out.println("f " + Thread.currentThread().getId() + " " + id);
			this.run();
		} else {
			// CAS loop
			for (; ; ) {
				SafeRunable tail = safeType.getTail();

				if (safeType.isHead(tail) && safeType.casTail(tail, this)) {
					// previous message is handled, order is
					// guaranteed.
//					System.out.println("r " + Thread.currentThread().getId() + " " + id);
					this.run();
					return;
				} else if (tail.casNext(this)) {
					safeType.casTail(tail, this);// fail is OK
					// successfully append to previous task
//					System.out.println("a " + Thread.currentThread().getId() + " " + id);
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
//			System.out.println("e " + Thread.currentThread().getId() + " " + id + " " + (next.get().next.get() != null));
			next.get().run();
		}
	}
	
	
	private boolean casNext(SafeRunable safeRunable) {
		return next.compareAndSet(null, safeRunable);
	}
	

	public SafeType getSafeType() {
		return safeType;
	}


	@Override
	public String toString() {
		return "SafeRunable [next=" + next + ", safeType="
				+ safeType + ", safeActor=" + safeActor + "]";
	}

}
