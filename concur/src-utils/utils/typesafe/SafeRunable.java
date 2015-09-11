package utils.typesafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的SafeRunable
 * @author Jake
 */
public class SafeRunable implements Runnable {
	
//	static AtomicInteger gen = new AtomicInteger();
//	
//	int id = gen.incrementAndGet();
	
	/** 后继节点 */
	private final AtomicReference<SafeRunable> next = new AtomicReference<SafeRunable>();
	
	/** 当前对象 */
	private final SafeType safeType;
	
	/** 当前任务 */
	private final SafeActor safeActor;
	
	protected SafeRunable(SafeType safeType, SafeActor safeActor) {
		this.safeType = safeType;
		this.safeActor = safeActor;
	}
	
	
	@Override
	public void run() {
		SafeRunable next = this;
		do {
			try {
				next.safeActor.run();
			} catch (Exception e) {
				next.safeActor.onException(e);
			}
		} while ((next = next.fetchNext()) != null);// 获取下一个任务
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

				if (isHead(tail) && safeType.casTail(tail, this)) {
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
	 * 获取下一个任务
	 */
	protected SafeRunable fetchNext() {
		if (!next.compareAndSet(null, this)) { // has more job to run
//			System.out.println("e " + Thread.currentThread().getId() + " " + id + " " + (next.get().next.get() != null));
			return next.get();
		}
		return null;
	}
	
	
	private boolean casNext(SafeRunable safeRunable) {
		return next.compareAndSet(null, safeRunable);
	}
	

	/**
	 * 判断节点是否为头节点
	 * @param safeRunable 节点
	 * @return
	 */
	public boolean isHead(SafeRunable safeRunable) {
		return safeRunable.next.get() == safeRunable;
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
