package dbcache.model;

import dbcache.conf.CacheRule;

/**
 * 索引Key对象
 * @author Jake
 * @date 2014年8月25日上午1:05:48
 */
public class IndexKey {

	/**
	 * 索引名
	 */
	private final String name;

	/**
	 * 索引值
	 */
	private final Object value;

	/**
	 * 索引键更新状态
	 */
	private volatile UpdateStatus updateStatus = UpdateStatus.TRANSIENT;


	/** 构造方法 */
	private IndexKey(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * 获取实例
	 * @param name 索引名
	 * @param value 索引值
	 * @return
	 */
	public static IndexKey valueOf(String name, Object value) {
		if (name == null) {
			throw new IllegalArgumentException("索引名不能为null");
		}
		return new IndexKey(name, value);
	}


	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public UpdateStatus getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(UpdateStatus updateStatus) {
		this.updateStatus = updateStatus;
	}

	@Override
	public int hashCode() {
		return CacheRule.getIndexIdKey(name, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexKey other = (IndexKey) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
