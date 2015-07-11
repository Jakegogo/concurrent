package utils.typesafe.extended;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全类型
 * 
 * @author Jake
 *
 */
public abstract class MultiSafeType {
	
	/**
	 * 上一次执行的Actor
	 */
	AtomicReference<MultiSafeRunable> head = new AtomicReference<MultiSafeRunable>();
	

}
