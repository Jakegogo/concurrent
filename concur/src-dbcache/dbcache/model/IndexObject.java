package dbcache.model;

import dbcache.utils.concurrent.ConcurrentLinkedHashMap8;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 索引缓存对象
 * @author Jake
 * @date 2014年9月21日下午5:56:10
 */
public class IndexObject<PK extends Comparable<PK> & Serializable> {


	/**
	 * 索引键
	 */
	private IndexKey indexKey;

	/**
	 * 索引键更新状态
	 */
	private AtomicReference<PersistStatus> updateStatus = new AtomicReference<PersistStatus>(PersistStatus.TRANSIENT);

	/**
	 * 是否持久状态
	 */
	private boolean doPersist = false;

	/**
	 * 索引区域缓存
	 */
	private ConcurrentMap<PK, Boolean> indexValues = new ConcurrentLinkedHashMap8<PK, Boolean>();


	/**
	 * 获取实例
	 * @param indexKey 索引键
	 * @return
	 */
	public static <PK extends Comparable<PK> & Serializable> IndexObject<PK> valueOf(IndexKey indexKey) {
		IndexObject<PK> indexObject = new IndexObject<PK>();
		indexObject.indexKey = indexKey;
		return indexObject;
	}

	/**
	 * 获取实例
	 * @param indexKey 索引键
	 * @param updateStatus 更新状态
	 * @return
	 */
	public static <PK extends Comparable<PK> & Serializable> IndexObject<PK> valueOf(IndexKey indexKey, PersistStatus updateStatus) {
		IndexObject<PK> indexObject = new IndexObject<PK>();
		indexObject.indexKey = indexKey;
		indexObject.updateStatus.set(updateStatus);
		return indexObject;
	}


	public IndexKey getIndexKey() {
		return indexKey;
	}

	public void setIndexKey(IndexKey indexKey) {
		this.indexKey = indexKey;
	}

	public PersistStatus getUpdateStatus() {
		return updateStatus.get();
	}

	public boolean compareAndSetUpdateStatus(PersistStatus preUpdateStatus, PersistStatus updateStatus) {
		return this.updateStatus.compareAndSet(preUpdateStatus, updateStatus);
	}

	public ConcurrentMap<PK, Boolean> getIndexValues() {
		return indexValues;
	}

	public void setIndexValues(ConcurrentMap<PK, Boolean> indexValues) {
		this.indexValues = indexValues;
	}


	public boolean isDoPersist() {
		return doPersist;
	}

	public void setDoPersist(boolean doPersist) {
		this.doPersist = doPersist;
	}
}
