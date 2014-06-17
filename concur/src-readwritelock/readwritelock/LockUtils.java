package readwritelock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;

import readwritelock.ObjectLock;
import readwritelock.ObjectLockHolder;


/**
 * 锁工具类
 * 
 * @author jake
 * 
 */
public class LockUtils {

	/** 锁持有者，用于避免重复的锁创建 */
	private static ObjectLockHolder holder = new ObjectLockHolder();

	/**
	 * 获取多个对象的同步锁
	 * 
	 * @param objects
	 *            要获得锁的对象或实体实例数组
	 * @return 可同时锁定参数对象的锁对象
	 * @throws IllegalArgumentException
	 *             对象数量为0时抛出
	 */
	public static ReadWriteLock getLock(Object... objects) {
		List<? extends ReadWriteLock> locks = loadLocks(objects);
		return new ReadWriteLinkedLock(locks);
	}

	/**
	 * 获取锁定顺序正确的锁列表
	 * 
	 * @param objects
	 *            要获得锁的对象或实体实例数组
	 * @return
	 */
	public static List<? extends ReadWriteLock> loadLocks(Object... objects) {
		// 获取锁并排序
		List<ObjectLock> locks = new ArrayList<ObjectLock>();
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
		List<ReadWriteLock> news = new ArrayList<ReadWriteLock>(locks.size() + idx.size());
		news.addAll(locks);
		Iterator<Integer> it = idx.descendingIterator();
		while (it.hasNext()) {
			Integer i = it.next();
			ObjectLock lock = locks.get(i);
			ReadWriteLock tieLock = holder.getTieLock(lock.getClz());
			news.add(i, tieLock);
		}
		return news;
	}
	
	
	/**
	 * 判断对象是否在写锁内
	 * @param object 对象
	 * @return
	 */
	public static boolean isHeldWriteByCurrentThread(Object object) {
		ObjectLock lock = holder.getLock(object);
		if (lock == null) {
			return false;
		}
		return lock.isWriteLockedByCurrentThread();
	}
	
	/**
	 * 取得锁类型实例数
	 * 
	 * @return Map<String, Object> 类名-实例数
	 */
	public static Map<String, Integer> getLockObjectCount() {
		return holder.getClassInstanceCount();
	}
	
	/**
	 * 输出加锁对象的线程堆栈信息
	 * 仅在slf4j为调试模式才会输出
	 */
	public static void printStackTrace(){
		holder.printStackTrace();
	}
	
}
