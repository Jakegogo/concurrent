package readwritelock.deadlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import readwritelock.ObjectLock;


/**
 * 可调式的读写锁
 * 
 * log4j开启调试模式后,可以通过记录最后一次加锁的位置，输出以进行分析死锁的原因
 * 
 * DebugReentrantReadWriteLock lock = new DebugReentrantReadWriteLock();
 * 注意:引用类型必须是子类类型(DebugReentrantReadWriteLock),否则无法覆盖静态内部类
 * 
 * @author jake
 * 
 */
public class DebugReentrantReadWriteLock extends ObjectLock implements
		Comparable<ObjectLock> {
	
	private static final long serialVersionUID = 3522589292644599325L;

	/** Inner class providing readlock */
	private final ReentrantReadWriteLock.ReadLock readerLock;
	/** Inner class providing writelock */
	private final ReentrantReadWriteLock.WriteLock writerLock;
	/** 上一次加读锁的线程堆栈信息 */
	private Map<Long, Stack<LockedThreadInfo>> lastReadLockThreadInfo = new ConcurrentHashMap<Long, Stack<LockedThreadInfo>>();
	/** 上一次加写锁的线程堆栈信息 */
	private Stack<LockedThreadInfo> lastWriteLockThreadInfo = new Stack<LockedThreadInfo>();
	/** 判断Stop the world */
	private static volatile boolean stopTheWorld = false;
	/** 停止所有线程的对象锁 */
	private static Object syncObject = new Object();
	/** 停止所有线程的超时时间 毫秒 */
	private static int STOP_THE_WORLD_TIME_OUT = 1000;
	/** 自增操作号ID */
	private static AtomicInteger operateId = new AtomicInteger(0);
	
	public DebugReentrantReadWriteLock(Object object) {
		this(false);
	}

	public DebugReentrantReadWriteLock(boolean fair) {
		super(fair);
		readerLock = new ReadLock(this);
		writerLock = new WriteLock(this);
	}

	public List<LockedThreadInfo> getLastWriteLockThreadInfo() {
		return new ArrayList<LockedThreadInfo>(lastWriteLockThreadInfo);
	}

	public void setLastWriteLockThreadInfo(
			Stack<LockedThreadInfo> lastWriteLockThreadInfo) {
		this.lastWriteLockThreadInfo = lastWriteLockThreadInfo;
	}

	public Map<Long, Stack<LockedThreadInfo>> getLastReadLockThreadInfo() {
		return lastReadLockThreadInfo;
	}

	public void setLastReadLockThreadInfo(
			Map<Long, Stack<LockedThreadInfo>> lastReadLockThreadInfo) {
		this.lastReadLockThreadInfo = lastReadLockThreadInfo;
	}

	public ReentrantReadWriteLock.WriteLock writeLock() {
		return writerLock;
	}

	public ReentrantReadWriteLock.ReadLock readLock() {
		return readerLock;
	}

	/**
	 * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
	 */
	public static class ReadLock extends ReentrantReadWriteLock.ReadLock {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8455762090620290564L;

		private DebugReentrantReadWriteLock debugReentrantReadWriteLock;

		protected ReadLock(DebugReentrantReadWriteLock arg0) {
			super(arg0);
			this.debugReentrantReadWriteLock = arg0;
		}

		@Override
		public void lock() {
			checkStopTheWorld();
			super.lock();
			saveLastReadLockThread();
		}

		@Override
		public boolean tryLock() {
			checkStopTheWorld();
			boolean result = super.tryLock();
			if (result) {
				saveLastReadLockThread();
			}
			return result;
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			checkStopTheWorld();
			super.lockInterruptibly();
			saveLastReadLockThread();
		}

		@Override
		public void unlock() {
			checkStopTheWorld();
			super.unlock();
			resetLastReadLockThread();
		}

		@Override
		public boolean tryLock(long timeout, TimeUnit unit)
				throws InterruptedException {
			checkStopTheWorld();
			boolean result = super.tryLock(timeout, unit);
			if (result) {
				saveLastReadLockThread();
			}
			return result;
		}

		/**
		 * 保存当前对象加锁的线程信息
		 */
		private void saveLastReadLockThread() {
			long threadId = Thread.currentThread().getId();
			Stack<LockedThreadInfo> currentThreadLastReadLockThreadInfo = debugReentrantReadWriteLock.lastReadLockThreadInfo
					.get(threadId);
			if (currentThreadLastReadLockThreadInfo == null) {
				currentThreadLastReadLockThreadInfo = new Stack<LockedThreadInfo>();
				debugReentrantReadWriteLock.lastReadLockThreadInfo.put(threadId, currentThreadLastReadLockThreadInfo);
			}
			currentThreadLastReadLockThreadInfo.push(LockedThreadInfo.valueOf(getOperateId(), 
					Thread.currentThread(), Thread.currentThread()
							.getStackTrace()));
		}

		/**
		 * 重置当前对象加锁的线程信息
		 */
		private void resetLastReadLockThread() {
			long threadId = Thread.currentThread().getId();
			Stack<LockedThreadInfo> currentThreadLastReadLockThreadInfo = debugReentrantReadWriteLock.lastReadLockThreadInfo
					.get(threadId);
			if (currentThreadLastReadLockThreadInfo != null) {
				currentThreadLastReadLockThreadInfo.pop();
			}
		}

	}

	/**
	 * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
	 */
	public static class WriteLock extends ReentrantReadWriteLock.WriteLock {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3564190501058125225L;

		private DebugReentrantReadWriteLock debugReentrantReadWriteLock;

		protected WriteLock(DebugReentrantReadWriteLock arg0) {
			super(arg0);
			this.debugReentrantReadWriteLock = arg0;
		}

		@Override
		public void lock() {
			checkStopTheWorld();
			super.lock();
			saveLastWriteLockThread();
		}

		@Override
		public boolean tryLock() {
			checkStopTheWorld();
			boolean result = super.tryLock();
			if (result) {
				saveLastWriteLockThread();
			}
			return result;
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			checkStopTheWorld();
			super.lockInterruptibly();
			saveLastWriteLockThread();
		}

		@Override
		public void unlock() {
			checkStopTheWorld();
			super.unlock();
			resetLastWriteLockThread();
		}

		@Override
		public boolean tryLock(long timeout, TimeUnit unit)
				throws InterruptedException {
			checkStopTheWorld();
			boolean result = super.tryLock(timeout, unit);
			if (result) {
				saveLastWriteLockThread();
			}
			return result;
		}

		/**
		 * 保存当前对象加锁的线程信息
		 */
		private void saveLastWriteLockThread() {
			debugReentrantReadWriteLock.lastWriteLockThreadInfo
					.push(LockedThreadInfo.valueOf(getOperateId(), Thread.currentThread(),
							Thread.currentThread().getStackTrace()));
		}

		/**
		 * 重置当前对象加锁的线程信息
		 */
		private void resetLastWriteLockThread() {
			debugReentrantReadWriteLock.lastWriteLockThreadInfo.pop();
		}

	}
	
	/**
	 * 检查并停止所有线程
	 */
	public static void checkStopTheWorld() {
		if(stopTheWorld) {
			synchronized(syncObject) {
				try {
					syncObject.wait(STOP_THE_WORLD_TIME_OUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 获取自增唯一操作ID
	 * @return 操作ID
	 */
	private static int getOperateId() {
		int id = operateId.incrementAndGet();
		if(id < 0) {
			operateId.set(0);
			return 0;
		}
		return id;
	}
	
	/**
	 * 停止所有使用锁的线程
	 */
	public static void stopTheWorld() {
		stopTheWorld = true;
	}
	
	/**
	 * 释放所有使用锁的线程
	 */
	public static void freeTheWorld() {
		stopTheWorld = false;
		synchronized(syncObject) {
			syncObject.notifyAll();
		}
	}

}
