package dbcache.model;

import java.io.Serializable;
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
	 * @param referent 实体
	 */
	public WeakCacheEntity(T referent) {
		super(referent);
	}


	/**
	 * 获取实例
	 * @param entity 实体对象
	 * @return
	 */
	public static <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> WeakCacheEntity<T, PK> valueOf(T entity) {
		return new WeakCacheEntity<T, PK>(entity);
	}


	@Override
	public PK getId() {
		T t = get();
		if(t != null) {
			return t.getId();
		}
		return null;
	}


	@Override
	public void setId(PK id) {
		T t = get();
		if(t != null) {
			t.setId(id);
		}
	}

}
