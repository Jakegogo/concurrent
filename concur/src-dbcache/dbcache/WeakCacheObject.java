package dbcache;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicIntegerArray;


/**
 * 弱引用实体缓存数据结构
 * @see CacheObject
 * @author jake
 * @date 2014-7-31-下午8:18:03
 */
public class WeakCacheObject<T extends IEntity<?>, R extends WeakCacheEntity<T,?>> extends CacheObject<T> {

	/**
	 * CacheObject的哈希码
	 */
	private int hashCode;


	/**
	 * 默认构造方法
	 */
	protected WeakCacheObject() {
	}


	/**
	 * 构造方法
	 *
	 * @param entity 实体
	 * @param clazz  类型
	 * @param proxyEntity WeakCacheEntity
	 */
	@SuppressWarnings("unchecked")
	public WeakCacheObject(T entity, Class<T> clazz, WeakReference<?> proxyEntity) {
		super(entity, clazz, (T) proxyEntity);
	}

	/**
	 * 构造方法
	 * @param entity 实体
	 * @param clazz 类型
	 * @param proxyEntity  WeakCacheEntity
	 * @param key 实体主键
	 * @param modifiedFields 修改过的字段的索引AtomicIntegerArray
	 */
	public WeakCacheObject(T entity, Class<T> clazz, T proxyEntity, Object key, AtomicIntegerArray modifiedFields) {
		super(entity, clazz, proxyEntity, modifiedFields);
		this.hashCode = key.hashCode();
	}


	@SuppressWarnings("unchecked")
	public T getEntity() {
		return ((R) entity).get();
	}


	@SuppressWarnings("unchecked")
	public T getProxyEntity() {
		return ((R) proxyEntity).get();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if (!(obj instanceof CacheObject)) {
			return false;
		}
		return obj.hashCode() == this.hashCode;
	}


}