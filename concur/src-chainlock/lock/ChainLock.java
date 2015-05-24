package lock;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * 锁链 -_-!!! 挺奇怪的名字
 * @author frank
 */
public class ChainLock {
	
	/** 当前的锁 */
	private final Lock current;
	/** 下一个锁节点 */
	private final ChainLock next;
	
	/**
	 * 根据给出的有序锁集合，创建一个锁链对象
	 * @param locks
	 * @throws IllegalArgumentException 锁对象数量为0时抛出
	 */
	public ChainLock(List<? extends Lock> locks) {
		if (locks == null || locks.size() == 0) {
			throw new IllegalArgumentException("构建锁链的锁数量不能为0");
		}
		this.current = locks.remove(0);
		if (locks.size() > 0) {
			this.next = new ChainLock(locks);
		} else {
			this.next = null;
		}
	}

	/**
	 * 对锁链中的多个锁对象，按顺序逐个加锁
	 */
	public void lock() {
		current.lock();
		if (next != null) {
			next.lock();
		}
	}

	/**
	 * 多锁链中的多个锁对象，逐个按顺序解锁
	 */
	public void unlock() {
		if (next != null) {
			next.unlock();
		}
		current.unlock();
	}

}
