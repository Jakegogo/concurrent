package readwritelock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * <b>维护多对象锁的链表锁</b>
 * 
 * <li>三种形式的锁获取（可中断、不可中断和定时）在其性能特征、排序保证或其他实现质量上可能会有所不同。而且，对于给定的 Lock 类，可能没有中断正在进行的
 * 锁获取的能力。因此，并不要求实现为所有三种形式的锁获取定义相同的保证或语义，也不要求其支持中断正在进行的锁获取。
 * 实现必需清楚地对每个锁定方法所提供的语义和保证进行记录
 * 。还必须遵守此接口中定义的中断语义，以便为锁获取中断提供支持：完全支持中断，或仅在进入方法时支持中断。</li>
 * 
 * <li>由于中断通常意味着取消，而通常又很少进行中断检查，因此，相对于普通方法返回而言，实现可能更喜欢响应某个中断。
 * 即使出现在另一个操作后的中断可能会释放线程锁时也是如此。实现应记录此行为。</li>
 * 
 * 
 * @author jake
 * 
 */
public class LinkedLock implements Lock {
	
	/** 当前的锁 */
	private final Lock current;
	/** 下一个锁节点 */
	private final LinkedLock next;

	/**
	 * 根据给出的有序锁集合，创建一个锁链对象
	 * 
	 * @param locks 锁集合
	 * @throws IllegalArgumentException
	 *             锁对象数量为0时抛出
	 */
	protected LinkedLock(List<? extends Lock> locks) {
		if (locks == null || locks.size() == 0) {
			throw new IllegalArgumentException("构建锁链的锁数量不能为0");
		}
		this.current = locks.remove(0);
		if (locks.size() > 0) {
			this.next = new LinkedLock(locks);
		} else {
			this.next = null;
		}
	}

	/**
	 * 对锁链中的多个锁对象，按顺序逐个加锁
	 */
	@Override
	public void lock() {
		current.lock();
		if (next != null) {
			next.lock();
		}
	}

	/**
	 * 多锁链中的多个锁对象，逐个按顺序解锁
	 */
	@Override
	public void unlock() {
		if (next != null) {
			next.unlock();
		}
		current.unlock();
	}

	/**
	 * 对锁链中的多个锁对象，按顺序逐个加锁, 并且在等待锁的过程中,允许在等待的情况下被中断 当某一个锁调用lock发生异常的情况下,
	 * 之前成功获取到的锁也会unlock解锁
	 * 
	 * @throws InterruptedException
	 *             当等待锁的线程被中断的时候抛出
	 */
	@Override
	public void lockInterruptibly() throws InterruptedException {
		current.lockInterruptibly();
		try {
			if (next != null)
				next.lockInterruptibly();
		} catch (InterruptedException e) {
			current.unlock();
			throw e;
		} catch (RuntimeException e) {
			current.unlock();
			throw e;
		}
	}

	/**
	 * 尝试对锁链中的多个锁对象，按顺序逐个加锁,无法获取锁则立即返回 当某一个锁调用lock发生异常或tryLock失败的情况下,
	 * 之前成功获取到的锁也会unlock解锁
	 * 
	 * @throws InterruptedException
	 *             当等待锁的线程被中断的时候抛出
	 * @return 成功获取锁 返回true 失败则 返回false
	 */
	@Override
	public boolean tryLock() {
		if (!current.tryLock())
			return false;
		try {
			if (next != null && !next.tryLock()) {
				current.unlock();
				return false;
			}
		} catch (RuntimeException e) {
			current.unlock();
			throw e;
		}
		return true;
	}

	/**
	 * 尝试在time时间内对锁链中的多个锁对象，按顺序逐个加锁, 当某一个锁调用lock发生异常或tryLock失败的情况下,
	 * 之前成功获取到的锁也会unlock解锁
	 * 
	 * @return 成功获取锁 返回true 失败则 返回false
	 * @param time 超时时间
	 * @param unit 时间单位
	 * @return 是否尝试加锁成功
	 * @throws InterruptedException 当等待锁的线程被中断的时候抛出
	 */
	@Override
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		long lastTime = System.currentTimeMillis() + unit.toMillis(time);
		if (!current.tryLock(time, unit))
			return false;
		long remainTime = lastTime - System.currentTimeMillis();
		try {
			if (next != null
					&& !next.tryLock(remainTime, TimeUnit.MILLISECONDS)) {
				current.unlock();
				return false;
			}
		} catch (InterruptedException e) {
			current.unlock();
			throw e;
		} catch (RuntimeException e) {
			current.unlock();
			throw e;
		}
		return true;
	}
	
	/**
	 * 注:不支持Condition
	 */
	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}

}
