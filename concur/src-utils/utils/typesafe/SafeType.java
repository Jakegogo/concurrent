package utils.typesafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全类型
 * @author Jake
 */
public abstract class SafeType {
	
	/** 上一次执行的Actor */
	private final AtomicReference<SafeRunable> tail = new AtomicReference<SafeRunable>();
	
	/**
	 * 获取尾节点
	 * @return
	 */
	public SafeRunable getTail() {
		return tail.get();
	}
	
	/**
	 * 追加到尾节点
	 * @param oldSafeRunnable
	 * @param safeRunable
	 * @return
	 */
	boolean casTail(SafeRunable oldSafeRunnable, SafeRunable safeRunable) {
		return tail.compareAndSet(oldSafeRunnable, safeRunable);
	}
	

}
