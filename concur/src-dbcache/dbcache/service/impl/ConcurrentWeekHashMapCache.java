package dbcache.service.impl;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import dbcache.ref.ConcurrentReferenceMap;
import dbcache.ref.ConcurrentReferenceMap.ReferenceKeyType;
import dbcache.ref.ConcurrentReferenceMap.ReferenceValueType;
import dbcache.service.Cache;

/**
 * ConcurrentWeekHashMap缓存容器
 * 
 * @author jake
 * @date 2014-8-1-下午8:30:34
 */
@Component("concurrentWeekHashMapCache")
public class ConcurrentWeekHashMapCache implements Cache {

	/**
	 * 初始容量
	 */
	private static final int DEFAULT_CAPACITY_OF_ENTITY_CACHE = 5000;

	/**
	 * 空值的引用
	 */
	private static final ValueWrapper NULL_HOLDER = new NullHolder();

	/**
	 * 缓存容器
	 */
	private final ConcurrentReferenceMap<Object, Object> store;

	/**
	 * 构造方法
	 */
	public ConcurrentWeekHashMapCache() {
		this(new ConcurrentReferenceMap<Object, Object>(
				ReferenceKeyType.STRONG, ReferenceValueType.WEAK,
				DEFAULT_CAPACITY_OF_ENTITY_CACHE));
	}

	/**
	 * 构造方法
	 * 
	 * @param concurrentReferenceMap
	 *            弱引用Map
	 */
	public ConcurrentWeekHashMapCache(
			ConcurrentReferenceMap<Object, Object> concurrentReferenceMap) {
		this.store = concurrentReferenceMap;
	}
	
	
	@Override
	public ValueWrapper get(Object key) {
		Object value = this.store.get(key);
		if(value == null && this.store.containsKey(key)) {
			return NULL_HOLDER;
		}
		ValueWrapper result = SimpleValueWrapper.valueOf(value);
		return result;
	}

	@Override
	public void put(Object key, Object value) {
		this.store.put(key, value);
	}

	@Override
	public ValueWrapper putIfAbsent(String key, Object value) {
		return SimpleValueWrapper.valueOf(this.store.putIfAbsent(key, value));
	}

	@Override
	public void evict(Object key) {
		this.store.remove(key);
	}

	@Override
	public void clear() {
		this.store.clear();
	}
	
	@SuppressWarnings("serial")
	private static class NullHolder implements ValueWrapper, Serializable {

		@Override
		public Object get() {
			return null;
		}
	}

	/**
	 * 缓存Value简单包装
	 * 
	 * @author jake
	 * @date 2014-7-31-下午8:29:49
	 */
	public static class SimpleValueWrapper implements ValueWrapper {
		
		/** 缓存的实体 */
		private final Object value;

		/**
		 * 构造方法
		 * 
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

		/**
		 * 获取实体
		 */
		public Object get() {
			return this.value;
		}

	}
	
	
	@Override
	public int getCachedSize() {
		return store.size();
	}

}
