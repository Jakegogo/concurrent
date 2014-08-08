package dbcache.ref;

import java.lang.ref.SoftReference;

/**
 * 带有回调清除功能的软引用对象。
 * @author <a href="http://www.agrael.cn">李翔</a>
 *
 * @param <T> 引用类型
 */
public abstract class FinalizableSoftReference<T> extends SoftReference<T> implements FinalizableReference<T> {

	/**
	 * 创建一个引用给定对象的新的带有回调清除功能的软引用。
	 * @param referent 新的带有回调清除功能的软引用将引用的对象。
	 */
	protected FinalizableSoftReference(T referent) {
		super(referent, FinalizableReferenceQueue.getInstance());
	}
	
	/**
	 * 创建一个引用给定对象的新的带有回调清除功能的软引用。
	 * @param referent 新的带有回调清除功能的软引用将引用的对象。
	 * @param finalizableReferenceQueue 该引用向其注册的可回调清理的队列。
	 */
	protected FinalizableSoftReference(T referent, FinalizableReferenceQueue finalizableReferenceQueue) {
		super(referent, finalizableReferenceQueue == null ? FinalizableReferenceQueue.getInstance() : finalizableReferenceQueue);
	}
}
