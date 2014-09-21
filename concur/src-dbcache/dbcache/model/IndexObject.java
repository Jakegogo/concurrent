package dbcache.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

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
	private volatile UpdateStatus updateStatus = UpdateStatus.TRANSIENT;

	/**
	 * 随影区域缓存
	 */
	private ConcurrentHashMap<PK, Boolean> indexValues = new ConcurrentHashMap<PK, Boolean>();


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
	public static <PK extends Comparable<PK> & Serializable> IndexObject<PK> valueOf(IndexKey indexKey, UpdateStatus updateStatus) {
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

	public UpdateStatus getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(UpdateStatus updateStatus) {
		this.updateStatus = updateStatus;
	}

	public ConcurrentHashMap<PK, Boolean> getIndexValues() {
		return indexValues;
	}

	public void setIndexValues(ConcurrentHashMap<PK, Boolean> indexValues) {
		this.indexValues = indexValues;
	}


}
