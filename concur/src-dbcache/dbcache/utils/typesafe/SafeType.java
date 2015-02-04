package dbcache.utils.typesafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全类型
 * 
 * @author Jake
 *
 */
public abstract class SafeType {
	
	/**
	 * 上一次执行的Actor
	 */
	AtomicReference<SafeRunable> head = new AtomicReference<SafeRunable>();
	
	/**
	 * 当前处理的线程
	 */
	volatile Thread currentThread;
	
	
	
	
}
