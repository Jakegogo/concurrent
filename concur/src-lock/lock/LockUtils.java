package lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;

/**
 * 锁工具对象
 * @author frank
 */
public class LockUtils {
	
	/** 锁持有者，用于避免重复的锁创建 */
	private static ObjectLockHolder holder = new ObjectLockHolder();
	
	/**
	 * 获取多个对象的同步锁
	 * @param objects 要获得锁的对象或实体实例数组
	 * @return 可同时锁定参数对象的锁对象
	 * @throws IllegalArgumentException 对象数量为0时抛出
	 */
	public static ChainLock getLock(Object...objects) {
		List<? extends Lock> locks = loadLocks(objects);
		return new ChainLock(locks);
	}

	/**
	 * 获取锁定顺序正确的锁列表
	 * @param objects 要获得锁的对象或实体实例数组
	 * @return
	 */
	public static List<? extends Lock> loadLocks(Object...objects) {

		if (objects == null) {
			throw new IllegalArgumentException("objects不能为NULL.");
		}

		if(objects.length == 1) {
			ObjectLock lock = holder.getLock(objects[0]);
			return Collections.singletonList(lock);
		}

		// 获取锁并排序
		List<ObjectLock> locks = new ArrayList<ObjectLock>(objects.length);
		for (Object obj : objects) {
			ObjectLock lock = holder.getLock(obj);
			locks.add(lock);
		}
		Collections.sort(locks);
		
		// 加入
		TreeSet<Integer> idx = new TreeSet<Integer>();
		Integer start = null;
		for (int i = 0; i < locks.size(); i++) {
			if (start == null) {
				start = i;
				continue;
			}
			ObjectLock lock1 = locks.get(start);
			ObjectLock lock2 = locks.get(i);
			if (lock1.isTie(lock2)) {
				idx.add(start);
				continue;
			}
			start = i;
		}
		
		if (idx.size() == 0) {
			return locks;
		}
		
		// 生成新的锁列表
		List<Lock> news = new ArrayList<Lock>(locks.size() + idx.size());
		news.addAll(locks);
		Iterator<Integer> it = idx.descendingIterator();
		while (it.hasNext()) {
			Integer i = it.next();
			ObjectLock lock = locks.get(i);
			Lock tieLock = holder.getTieLock(lock.getClz());
			news.add(i, tieLock);
		}
		return news;
	}
	
	/**
	 * 取得锁类型实例数
	 * @return String
	 */
	public static String getLockObjectCount() {
		return holder.getClassInstanceCount();
	}
	
}
