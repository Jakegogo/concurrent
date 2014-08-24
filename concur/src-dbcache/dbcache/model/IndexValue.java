package dbcache.model;

import java.io.Serializable;

/**
 * 索引值对象
 * @author Jake
 * @date 2014年8月25日上午1:07:14
 */
public class IndexValue<PK extends Comparable<PK> & Serializable> {
	
	/**
	 * 索引键
	 */
	private volatile IndexKey<PK> indexKey;
	
	/**
	 * 索引值对应的实体Id
	 */
	private final PK id;
	
	/**
	 * 索引值更新状态
	 */
	private volatile UpdateStatus updateStatus = UpdateStatus.PERSIST;
	
	/**
	 * 构造方法
	 * @param id 实体Id
	 * @param indexKey 索引键
	 */
	public IndexValue(PK id, IndexKey<PK> indexKey) {
		this.id = id;
		this.indexKey = indexKey;
	}
	
	/**
	 * 获取实例
	 * @param id 实体Id
	 * @param indexKey 索引键
	 * @return
	 */
	public static <PK extends Comparable<PK> & Serializable> IndexValue<PK> valueOf(PK id, IndexKey<PK> indexKey) {
		return new IndexValue<PK>(id, indexKey);
	}

	public IndexKey<PK> getIndexKey() {
		return indexKey;
	}

	public void setIndexKey(IndexKey<PK> indexKey) {
		this.indexKey = indexKey;
	}

	public PK getId() {
		return id;
	}

	public UpdateStatus getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(UpdateStatus updateStatus) {
		this.updateStatus = updateStatus;
	}
	
}
