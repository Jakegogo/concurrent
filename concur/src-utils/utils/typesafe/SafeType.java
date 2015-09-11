package utils.typesafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全类型
 * @author Jake
 */
public abstract class SafeType {
	
	/** 上一次执行的Actor */
	final AtomicReference<SafeRunable> tail = new AtomicReference<SafeRunable>();

	public SafeRunable getTail() {
		return tail.get();
	}
	
	public boolean isHead(SafeRunable tail) {
		return tail.next.get() == tail;
	}

	boolean casTail(SafeRunable last, SafeRunable safeRunable) {
		return tail.compareAndSet(last, safeRunable);
	}
	

}
