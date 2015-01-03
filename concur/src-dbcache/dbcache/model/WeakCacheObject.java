package dbcache.model;

import dbcache.conf.JsonConverter;
import dbcache.support.asm.ValueGetter;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collection;


/**
 * 实体弱引用缓存辅助类
 *
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
	 * @param entity
	 *            实体
	 * @param id
	 *            主键
	 * @param clazz
	 *            类型
	 */
	@SuppressWarnings("unchecked")
	public WeakCacheObject(T entity, Serializable id, Class<T> clazz, WeakReference<?> proxyEntity) {
		super(entity, id, clazz, (T) proxyEntity);
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param id
	 *            主键
	 * @param clazz
	 *            类型
	 */
	@SuppressWarnings("unchecked")
	public WeakCacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity, Object key, Collection<ValueGetter<T>> indexes, Collection<JsonConverter<T>> jsonConverters) {
		super(entity, id, clazz, proxyEntity, indexes, jsonConverters);
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
		return obj.hashCode() == this.hashCode;
	}


}