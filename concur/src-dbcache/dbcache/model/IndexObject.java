package dbcache.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private volatile PersistStatus updateStatus = PersistStatus.TRANSIENT;

	/**
	 * 随影区域缓存
	 */
	private ConcurrentHashMap<PK, Boolean> indexValues = new ConcurrentHashMap<PK, Boolean>();

	/**
	 * 索引缓存持有锁
	 */
	private ReadWriteLock lock = new ReentrantReadWriteLock();


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
		indexObject.updateStatus = updateStatus;
		return indexObject;
	}


	public IndexKey getIndexKey() {
		return indexKey;
	}

	public void setIndexKey(IndexKey indexKey) {
		this.indexKey = indexKey;
	}

	public PersistStatus getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(PersistStatus updateStatus) {
		this.updateStatus = updateStatus;
	}

	public ConcurrentHashMap<PK, Boolean> getIndexValues() {
		return indexValues;
	}

	public void setIndexValues(ConcurrentHashMap<PK, Boolean> indexValues) {
		this.indexValues = indexValues;
	}

	public ReadWriteLock getLock() {
		return lock;
	}

	public void setLock(ReadWriteLock lock) {
		this.lock = lock;
	}


}
