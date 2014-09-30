package dbcache.model;

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * 软引用实体缓存对象
 * @author jake
 *
 * @param <T>
 * @param <PK>
 */
public class WeakCacheEntity<T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> extends WeakReference<T> implements IEntity<PK> {

	/**
	 * 弱引用实体Key
	 */
	private Object key;

	/**
	 * @param referent 实体
	 * @param referenceQueue 回收队列
	 */
	public WeakCacheEntity(T referent, ReferenceQueue<T> referenceQueue, Object key) {
		super(referent, referenceQueue);
		this.key = key;
	}


	/**
	 * 获取实例
	 * @param entity 实体对象
	 * @param referenceQueue 回收队列
	 * @return
	 */
	public static <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> WeakCacheEntity<T, PK> valueOf(T entity, ReferenceQueue<T> referenceQueue, Object key) {
		return new WeakCacheEntity<T, PK>(entity, referenceQueue, key);
	}


	@Override
	public PK getId() {
		IEntity<PK> t = get();
		if(t != null) {
			return t.getId();
		}
		return null;
	}


	@Override
	public void setId(PK id) {
		IEntity<PK> t = get();
		if(t != null) {
			t.setId(id);
		}
	}


	public Object getKey() {
		return key;
	}


}
