package readwritelock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dbcache.utils.JsonUtils;
import readwritelock.checkdeadlock.DebugReentrantReadWriteLock;
import readwritelock.checkdeadlock.LockedThreadInfo;


/**
 * 对象锁持有者
 * @author jake
 */
@SuppressWarnings("rawtypes")
public class ObjectLockHolder {
	
	private static final Logger log = LoggerFactory.getLogger(ObjectLockHolder.class);
	
	/** 堆栈输出信息日期格式 */
	private static final String STACK_TRACE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 单一类的锁持有者
	 * @author frank
	 */
	public class Holder {

		/** 持有的对象类型，先放在应该会有用的 */
		@SuppressWarnings("unused")
		private final Class clz;
		/** 类型唯一锁 */
		private final ReadWriteLock tieLock = new ReentrantReadWriteLock();
		/** 对象实例与其对应的锁缓存 */
		private final WeakHashMap<Object, ObjectLock> locks = new WeakHashMap<Object, ObjectLock>();
		/** 持有者内置读写锁 */
		private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		
		/**
		 * 创建一个持有者实例
		 * @param clz
		 */
		public Holder(Class clz) {
			this.clz = clz;
		}

		/**
		 * 获取对象锁
		 * @param object
		 * @return
		 */
		public ObjectLock getLock(Object object) {
			Lock lock = this.lock.readLock();
			try {
				lock.lock();
				ObjectLock result = locks.get(object);
				if (result != null) {
					return result;
				}
			} finally {
				lock.unlock();
			}
			return createLock(object);
		}

		/**
		 * 创建对象锁
		 * @param object
		 * @return
		 */
		private ObjectLock createLock(Object object) {
			Lock lock = this.lock.writeLock();
			try {
				lock.lock();
				ObjectLock result = locks.get(object);
				if (result != null) {
					return result;
				}
				if(log.isWarnEnabled()) {
					result = new DebugReentrantReadWriteLock(object);
				} else {
					result = new ObjectLock(object);
				}
				locks.put(object, result);
				return result;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 获取类型唯一锁
		 * @return
		 */
		public ReadWriteLock getTieLock() {
			return tieLock;
		}

		/**
		 * 获取锁的数量
		 * @return
		 */
		public int count() {
			return locks.size();
		}
		
		/**
		 * 获取维护的所有锁的迭代器
		 * @return 对象-锁
		 */
		public Iterator<Entry<Object, ObjectLock>> lockIterator() {
			return new HashMap<Object, ObjectLock>(locks).entrySet().iterator();
		}
	}

	/** 持有者集合 */
	private ConcurrentHashMap<Class, Holder> holders = new ConcurrentHashMap<Class, Holder>();

	/**
	 * 获取指定对象实例的对象锁
	 * @param object 要获取锁的对象实例
	 * @return
	 */
	public ObjectLock getLock(Object object) {
		Holder holder = getHolder(object.getClass());
		ObjectLock lock = holder.getLock(object);
		return lock;
	}

	/**
	 * 获取某类实例的锁持有者
	 * @param clz 指定类型
	 * @return
	 */
	private Holder getHolder(Class clz) {
		Holder holder = holders.get(clz);
		if (holder != null) {
			return holder;
		}
		holders.putIfAbsent(clz, new Holder(clz));
		return holders.get(clz);
	}

	/**
	 * 获取指定类型的类型唯一锁
	 * @param clz 指定类型
	 * @return
	 */
	public ReadWriteLock getTieLock(Class clz) {
		Holder holder = getHolder(clz);
		return holder.getTieLock();
	}

	/**
	 * 获取指定类型的锁的数量
	 * @param clz
	 * @return
	 */
	public int count(Class<?> clz) {
		if (holders.containsKey(clz)) {
			Holder holder = getHolder(clz);
			return holder.count();
		}
		return 0;
	}
	
	/**
	 * 获取类型实例数
	 * @return String
	 */
	public Map<String, Integer> getClassInstanceCount() {
		Map<String, Integer> map = new HashMap<String, Integer>();

		for (Entry<Class, Holder> entry: holders.entrySet()) {
			Class clazz = entry.getKey();
			Holder holder = entry.getValue();
			
			map.put(clazz.getName(), holder.count());
		}
		
		return map;
	}
	
	/**
	 * 获取加锁对象的堆栈
	 * @return String
	 */
	public void printStackTrace() {
		
		if(log.isWarnEnabled()) {
			//停止所有锁操作的线程
			DebugReentrantReadWriteLock.stopTheWorld();
			SimpleDateFormat dateFormat = new SimpleDateFormat(STACK_TRACE_DATE_FORMAT);
			log.warn(dateFormat.format(new Date()));
			log.warn("LockUtils.printStackTrace()");
			for (Entry<Class, Holder> entry: holders.entrySet()) {
				
				Class clazz = entry.getKey();
				Holder holder = entry.getValue();
				log.warn(clazz.getName() + ":");
				for(Iterator<Entry<Object, ObjectLock>> it = holder.lockIterator();it.hasNext();) {
					
					StringBuilder sb = new StringBuilder();
					boolean show = false;
					Entry<Object, ObjectLock> entry1 = it.next();
					sb.append("\t" + entry1.getKey() + " : " + JsonUtils.object2JsonString(entry1.getKey()));
					DebugReentrantReadWriteLock lock = (DebugReentrantReadWriteLock)entry1.getValue();
					//遍历写锁加锁线程堆栈
					if(lock.getLastReadLockThreadInfo() != null) {
						StringBuilder readLockStr = new StringBuilder();
						boolean showReadStr = false;
						for(Entry<Long, Stack<LockedThreadInfo>> entry2 : lock.getLastReadLockThreadInfo().entrySet()) {
							if(entry2.getValue() == null || entry2.getValue().size() == 0) {
								continue;
							}
							List<LockedThreadInfo> readThreadStack = new ArrayList<LockedThreadInfo>(entry2.getValue());
							for(LockedThreadInfo lockedThreadInfo : readThreadStack) {
								readLockStr.append("\n\t\tread  : operateID:" + lockedThreadInfo.getOperateId() + "-ThreadID:" + entry2.getKey());
								readLockStr.append("-Date[" + lockedThreadInfo.getTime().getTime() + "," + dateFormat.format(lockedThreadInfo.getTime()) + "]");
								StackTraceElement[] trace = lockedThreadInfo.getLastLockStackTrace();
								boolean m = false;
					            for (int i=0; i < trace.length; i++) {
					            	if(m) {
					            		readLockStr.append("\n\t\t\tat " + trace[i]);
					            	}
					            	if (LinkedLock.class.getName().equals(trace[i].getClassName())) {
					            		m = true;
					            	}
					            }
					            show = true;
					            showReadStr = true;
							}
						}
						if(showReadStr) {
							sb.append(readLockStr);
						}
					}
					//遍历写锁加锁线程堆栈
					if(lock.getLastWriteLockThreadInfo() != null) {
						for(LockedThreadInfo lockedThreadInfo : lock.getLastWriteLockThreadInfo()) {
							sb.append("\n\t\twrite : operateID:" + lockedThreadInfo.getOperateId() + "-ThreadID:" + lockedThreadInfo.getLastLockThread().getId());
							sb.append("-Date[" + lockedThreadInfo.getTime().getTime() + "," + dateFormat.format(lockedThreadInfo.getTime()) + "]");
							StackTraceElement[] trace = lockedThreadInfo.getLastLockStackTrace();
							boolean m = false;
				            for (int i=0; i < trace.length; i++) {
				            	if(m) {
				            		sb.append("\n\t\t\tat " + trace[i]);
				            	}
				            	if (LinkedLock.class.getName().equals(trace[i].getClassName())) {
				            		m = true;
				            	}
				            }
							show = true;
						}
					}
					if(show) {
						log.warn(sb.toString());
					}
				}
			}
			//释放所有锁操作的线程
			DebugReentrantReadWriteLock.freeTheWorld();
		}
	}
	
	
	/**
	 * 打印持有锁对象
	 */
	public void printLockObject() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(STACK_TRACE_DATE_FORMAT);
		log.warn(dateFormat.format(new Date()));
		log.warn("LockUtils.printLockObject()");
		for (Entry<Class, Holder> entry: holders.entrySet()) {
			Class clazz = entry.getKey();
			Holder holder = entry.getValue();
			log.warn(clazz.getName() + ":");
			for(Iterator<Entry<Object, ObjectLock>> it = holder.lockIterator();it.hasNext();) {
				Entry<Object, ObjectLock> entry1 = it.next();
				log.warn("\t" + entry1.getKey() + " : " + JsonUtils.object2JsonString(entry1.getKey()));
			}
		}
		
	}
	
	
}
