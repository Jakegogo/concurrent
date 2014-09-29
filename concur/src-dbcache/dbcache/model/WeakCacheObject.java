package dbcache.model;

import java.io.Serializable;
import java.lang.ref.WeakReference;


/**
 * 实体弱引用缓存辅助类
 *
 * @author jake
 * @date 2014-7-31-下午8:18:03
 */
public class WeakCacheObject<T extends IEntity<?>> extends CacheObject<T> {

	/**
	 * CacheObject的哈希码
	 */
	private int hashCode;

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
	public WeakCacheObject(WeakCacheEntity<T, ?> entity, Serializable id, Class<T> clazz, WeakReference<T> proxyEntity) {
		super((T) entity, id, clazz, (T) proxyEntity);
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
	 * @param updateStatus
	 *            更新方式
	 */
	@SuppressWarnings("unchecked")
	public WeakCacheObject(WeakCacheEntity<T, ?> entity, Serializable id, Class<T> clazz, WeakReference<T> proxyEntity,
			UpdateStatus updateStatus) {
		super((T) entity, id, clazz, (T) proxyEntity, updateStatus);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T getEntity() {
		return (T) ((WeakCacheEntity) entity).get();
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T getProxyEntity() {
		return (T) ((WeakCacheEntity) proxyEntity).get();
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