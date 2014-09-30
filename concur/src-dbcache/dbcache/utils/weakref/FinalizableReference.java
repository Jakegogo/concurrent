package dbcache.utils.weakref;

/**
 * 带有回调清除功能的引用类型。
 * @author <a href="http://www.agrael.cn">李翔</a>
 *
 * @param <T> 引用对象的类型
 */
public interface FinalizableReference<T> {

	/**
	 * 清除动作的回调方法。在该实现类包装的引用对象被垃圾回收器回收时调用该方法。
	 */
	void finalizeReferent();

}
