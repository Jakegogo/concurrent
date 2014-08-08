package dbcache.ref;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 用做 ReferenceMap 的清除引用的引用队列。
 * @author <a href="http://www.agrael.cn">李翔</a>
 *
 */
public class FinalizableReferenceQueue extends ReferenceQueue<Object> {
	
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(FinalizableReferenceQueue.class);
	
	private static final Object LOCK = new Object();
	
	private int i;
	
	/**
	 * 构造一个新的清除引用的引用队列。
	 */
	public FinalizableReferenceQueue() {
		synchronized (LOCK) {
			start("FinalizableReferenceQueue#" + ++i);
		}
	}
	
	/**
	 * 构造一个新的清除引用的引用队列。
	 * @param name 引用队列的名称，该名称用做清理的守护线程的名称。
	 */
	public FinalizableReferenceQueue(String name) {
		start(name);
	}
	
	
	private static class FinalizeReferenceQueueInstance {
		private static final FinalizableReferenceQueue INSTANCE = new FinalizableReferenceQueue("FinalizableReferenceQueue");
	}

	/**
	 * 得到 FinalizableReferenceQueue 的单例。
	 * @return FinalizableReferenceQueue 的单例。
	 */
	public static FinalizableReferenceQueue getInstance() {
		return FinalizeReferenceQueueInstance.INSTANCE;
	}
	
	/**
	 * 执行引用的清除工作。
	 * @param reference 要执行清除工作的引用。
	 */
	void cleanUp(Reference<?> reference) {
		try {
			((FinalizableReference<?>) reference).finalizeReferent();
		} catch (Throwable t) {
			logger.error("清除引用时发生错误", t);
		}
	}
	
	/**
	 * 开始垃圾回收引用监视。
	 */
	void start(String name) {
		Thread thread = new Thread(name) {
			
			@Override
			public void run() {
				while (true) {
					try {
						cleanUp(remove());
					} catch (InterruptedException e) {
						// 不处理
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
		if (logger.isDebugEnabled()) {
			logger.debug("垃圾回收引用监视器[" + name + "]开始工作。");
		}
	}
}