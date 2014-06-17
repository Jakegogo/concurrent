package readwritelock.deadlock;

import java.util.Date;

/**
 * 加锁的线程堆栈信息
 * 
 * @author jake
 * 
 */
public class LockedThreadInfo {
	
	/** 操作序号 */
	private int operateId;

	/** 加锁的线程堆栈信息 */
	private StackTraceElement[] lastLockStackTrace;

	/** 加锁的线程 */
	private Thread lastLockThread;
	
	/** 加锁时间 */
	private Date time;
	
	/**
	 * 获取实例
	 * @param lastLockThread 加锁的线程堆栈信息
	 * @param lastLockStackTrace 加锁的线程
	 * @return
	 */
	public static LockedThreadInfo valueOf(int operateId, Thread lastLockThread, StackTraceElement[] lastLockStackTrace) {
		LockedThreadInfo lockedThreadInfo = new LockedThreadInfo();
		lockedThreadInfo.operateId = operateId;
		lockedThreadInfo.lastLockThread = lastLockThread;
		lockedThreadInfo.lastLockStackTrace = lastLockStackTrace;
		lockedThreadInfo.time = new Date();
		return lockedThreadInfo;
	}

	public int getOperateId() {
		return operateId;
	}

	public void setOperateId(int operateId) {
		this.operateId = operateId;
	}

	public StackTraceElement[] getLastLockStackTrace() {
		return lastLockStackTrace;
	}

	public Thread getLastLockThread() {
		return lastLockThread;
	}

	public void setLastLockStackTrace(StackTraceElement[] lastLockStackTrace) {
		this.lastLockStackTrace = lastLockStackTrace;
	}

	public void setLastLockThread(Thread lastLockThread) {
		this.lastLockThread = lastLockThread;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

}
