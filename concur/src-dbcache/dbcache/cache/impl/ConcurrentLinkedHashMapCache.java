package dbcache.cache.impl;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

import dbcache.cache.CacheUnit;
import dbcache.cache.ValueWrapper;
import utils.collections.concurrent.ConcurrentReferenceHashMap;
import utils.collections.concurrent.ConcurrentReferenceHashMap.ReferenceType;

import org.springframework.stereotype.Component;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Google ConcurrentLinkedHashMap缓存容器
 * 如果外部持有缓存对象的引用,对象将不会被回收
 * @author jake
 * @date 2014-7-31-下午8:24:23
 */
@Component("concurrentLinkedHashMapCache")
public class ConcurrentLinkedHashMapCache implements CacheUnit {

	/**
	 * 缺省实体缓存最大容量
	 */
	private static final int DEFAULT_MAX_CAPACITY_OF_ENTITY_CACHE = 100000;

	/**
	 * 缓存名称
	 */
	private String name;

	/**
	 * 空值的引用
	 */
	private static final ValueWrapper NULL_HOLDER = new NullHolder();

	/**
	 * 缓存容器
	 */
	private ConcurrentMap<Object, ValueWrapper> store;

	/**
	 * 已经回收的实体
	 */
	private ConcurrentReferenceHashMap<Object, Object> evictions;


	/**
	 * 初始化
	 * @param name
	 * @param entityCacheSize
	 * @param concurrencyLevel
	 */
	public void init(String name, int entityCacheSize, int concurrencyLevel) {

		this.name = name;
		this.evictions = new ConcurrentReferenceHashMap<Object, Object>(ReferenceType.STRONG, ReferenceType.WEAK);

		this.store = new ConcurrentLinkedHashMap.Builder<Object, ValueWrapper>()
				.maximumWeightedCapacity(entityCacheSize > 0 ? entityCacheSize : DEFAULT_MAX_CAPACITY_OF_ENTITY_CACHE)
				.concurrencyLevel(concurrencyLevel).listener(new EvictionListener<Object, ValueWrapper>() {

					@Override
					public void onEviction(Object key, ValueWrapper value) {
						if (value.get() != null) {
							evictions.put(key, value.get());
						}
					}

				}).build();
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
		if(value != null) {
			return (ValueWrapper) fromStoreValue(value);
		}
		value = this.evictions.get(key);
		if(value != null) {
			// 添加到主缓存
			this.putIfAbsent(key, value);
			// 从临时缓存中移除
			this.evictions.remove(key);

			return this.get(key);
		}
		return null;
	}
	

	@Override
	public ValueWrapper put(Object key, Object value) {
		return this.store.put(key, toStoreValue(value));
	}


	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		ValueWrapper oldValueWrapper = this.store.putIfAbsent(key, toStoreValue(value));
		if(oldValueWrapper == NULL_HOLDER) {
			this.replace(key, null, value);
		}
		return this.get(key);
	}

	@Override
	public ValueWrapper replace(Object key, Object oldValue, Object newValue) {
		this.store.replace(key, toStoreValue(oldValue), toStoreValue(newValue));
		return this.get(key);
	}


	@Override
	public ValueWrapper evict(Object key) {
		ValueWrapper value = this.store.remove(key);
		Object value1 = this.evictions.remove(key);
		return value == null ? SimpleValueWrapper.valueOf(value1) : value;
	}

	@Override
	public ValueWrapper remove(Object key) {
		return this.store.remove(key);
	}

	@Override
	public void clear() {
		this.store.clear();
		this.evictions.clear();
	}


	/**
	 * Convert the given value from the internal store to a user value
	 * returned from the get method (adapting <code>null</code>).
	 * @param storeValue the store value
	 * @return the value to return to the user
	 */
	private Object fromStoreValue(Object storeValue) {
		return storeValue;
	}

	/**
	 * Convert the given user value, as passed into the put method,
	 * to a value in the internal store (adapting <code>null</code>).
	 * @param userValue the given user value
	 * @return the value to store
	 */
	private ValueWrapper toStoreValue(Object userValue) {
		if (userValue == null) {
			return NULL_HOLDER;
		}
		return SimpleValueWrapper.valueOf(userValue);
	}


	@Override
	public int getCachedSize() {
		return store.size();
	}

	@Override
	public String getName() {
		return this.name;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public ReferenceQueue getReferencequeue() {
		return null;
	}


}
