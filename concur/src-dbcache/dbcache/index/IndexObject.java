package dbcache.index;

import utils.collections.concurrent.ConcurrentLinkedHashMap8;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * 索引缓存对象
 * @author Jake
 * @date 2014年9月21日下午5:56:10
 */
public class IndexObject<PK extends Comparable<PK> & Serializable> implements Comparable<IndexObject<PK>> {

	/** 索引键 */
	private IndexKey indexKey;

	/**  索引区域缓存  */
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


	public IndexKey getIndexKey() {
		return indexKey;
	}

	public void setIndexKey(IndexKey indexKey) {
		this.indexKey = indexKey;
	}

	public ConcurrentMap<PK, Boolean> getIndexValues() {
		return indexValues;
	}

	public void setIndexValues(ConcurrentMap<PK, Boolean> indexValues) {
		this.indexValues = indexValues;
	}

	public IndexObject<PK> put(PK key, Boolean aBoolean) {
		this.indexValues.put(key, aBoolean);
		return this;
	}

	public IndexObject<PK> remove(Object key) {
		this.indexValues.remove(key);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IndexObject)) return false;

		IndexObject that = (IndexObject) o;

		if (!indexKey.equals(that.indexKey)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return indexKey.hashCode();
	}

	@Override
	public int compareTo(IndexObject<PK> o) {
		if (this == o) return 0;
		return this.indexKey.compareTo(o.indexKey);
	}
}
