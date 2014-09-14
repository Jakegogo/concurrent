package dbcache.service.impl;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import dbcache.service.Cache;
import dbcache.service.DbRuleService;

/**
 * ConcurrentLinkedHashMap缓存容器
 * @author jake
 * @date 2014-7-31-下午8:24:23
 */
@Component("concurrentLinkedHashMapCache")
public class ConcurrentLinkedHashMapCache implements Cache {

	@Autowired
	private DbRuleService dbRuleService;

	/**
	 * 缺省实体缓存最大容量
	 */
	private static final int DEFAULT_MAX_CAPACITY_OF_ENTITY_CACHE = 100000;

	/**
	 * 空值的引用
	 */
	private static final ValueWrapper NULL_HOLDER = new NullHolder();

	/**
	 * 缓存容器
	 */
	private ConcurrentMap<Object, ValueWrapper> store;



	/**
	 * 初始化
	 */
//	@PostConstruct
	public void init() {
		int entityCacheSize = dbRuleService.getEntityCacheSize();
		if(entityCacheSize <= 0) {
			entityCacheSize = DEFAULT_MAX_CAPACITY_OF_ENTITY_CACHE;
		}
		this.store = new ConcurrentLinkedHashMap.Builder<Object, ValueWrapper>().maximumWeightedCapacity(entityCacheSize).build();
	}


	/**
	 * 初始化
	 * @param entityCacheSize
	 * @param concurrencyLevel
	 */
	public void init(int entityCacheSize, int concurrencyLevel) {
		this.store = new ConcurrentLinkedHashMap.Builder<Object, ValueWrapper>()
				.maximumWeightedCapacity(entityCacheSize)
				.concurrencyLevel(concurrencyLevel).build();
	}

	/**
	 * 构造方法 使用默认的cacheSize
	 */
	public ConcurrentLinkedHashMapCache() {
	}


	/**
	 * 构造方法
	 * @param cacheSize 默认最大容量
	 */
	public ConcurrentLinkedHashMapCache(int cacheSize) {
		this(new ConcurrentLinkedHashMap.Builder<Object, ValueWrapper>().maximumWeightedCapacity(cacheSize).build());
	}


	/**
	 * 构造方法
	 * @param store Map
	 */
	public ConcurrentLinkedHashMapCache(ConcurrentLinkedHashMap<Object, ValueWrapper> store) {
		this.store = store;
	}


	@Override
	public ValueWrapper get(Object key) {
		Object value = this.store.get(key);
		return (ValueWrapper) fromStoreValue(value);
	}


	@Override
	public void put(Object key, Object value) {
		this.store.put(key, toStoreValue(SimpleValueWrapper.valueOf(value)));
	}


	@Override
	public ValueWrapper putIfAbsent(String key, Object value) {
		return this.store.putIfAbsent(key, toStoreValue(SimpleValueWrapper.valueOf(value)));
	}

	@Override
	public void evict(Object key) {
		this.store.remove(key);
	}

	@Override
	public void clear() {
		this.store.clear();
	}


	/**
	 * Convert the given value from the internal store to a user value
	 * returned from the get method (adapting <code>null</code>).
	 * @param storeValue the store value
	 * @return the value to return to the user
	 */
	protected Object fromStoreValue(Object storeValue) {
		if (storeValue == NULL_HOLDER) {
			return null;
		}
		return storeValue;
	}

	/**
	 * Convert the given user value, as passed into the put method,
	 * to a value in the internal store (adapting <code>null</code>).
	 * @param userValue the given user value
	 * @return the value to store
	 */
	protected ValueWrapper toStoreValue(ValueWrapper userValue) {
		if (userValue == null) {
			return NULL_HOLDER;
		}
		return userValue;
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
	 * @author jake
	 * @date 2014-7-31-下午8:29:49
	 */
	public static class SimpleValueWrapper implements ValueWrapper {

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
