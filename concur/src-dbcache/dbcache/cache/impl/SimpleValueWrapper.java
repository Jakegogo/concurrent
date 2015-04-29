package dbcache.cache.impl;

import dbcache.cache.ValueWrapper;

/**
 * 缓存Value简单包装
 * @author jake
 * @date 2014-7-31-下午8:29:49
 */
public class SimpleValueWrapper implements ValueWrapper {

	private final Object value;


	/**
	 * 构造方法
	 * @param value 实体(可以为空)
	 */
	public SimpleValueWrapper(Object value) {
		this.value = value;
	}

	/**
	 * 获取实例
	 * @param value 值
	 * @return
	 */
	public static SimpleValueWrapper valueOf(Object value) {
		if(value == null) {
			return null;
		}
		return new SimpleValueWrapper(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof ValueWrapper)) {
			return false;
		}

		SimpleValueWrapper that = (SimpleValueWrapper) o;

		if (this.value == null) {
			return that.value == null;
		}

		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	/**
	 * 获取实体
	 */
	public Object get() {
		return this.value;
	}

}