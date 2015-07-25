package dbcache.cache.impl;

import dbcache.cache.CacheUnit;
import dbcache.cache.ValueWrapper;
import dbcache.utils.CacheUtils;
import org.springframework.stereotype.Component;
import utils.collections.concurrent.ConcurrentLRUCache;
import utils.collections.concurrent.ConcurrentReferenceHashMap;
import utils.collections.concurrent.ConcurrentReferenceHashMap.ReferenceType;

import java.lang.ref.ReferenceQueue;

/**
 * Apache ConcurrentLRUCache缓存容器
 * 如果外部持有缓存对象的引用,对象将不会被回收
 * @author jake
 * @date 2014-7-31-下午8:24:23
 */
@Component("concurrentLruHashMapCache")
public class ConcurrentLruHashMapCache implements CacheUnit {

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
	private ConcurrentLRUCache<Object, ValueWrapper> store;

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

		int size = (entityCacheSize * 4 + 3) / 3;
		this.store = new ConcurrentLRUCache<Object, ValueWrapper>(size, entityCacheSize, (int) Math
				.floor((entityCacheSize + size) / 2), (int) Math
				.ceil(0.75f * size), true, false, concurrencyLevel, new ConcurrentLRUCache.EvictionListener<Object, ValueWrapper>() {

			@Override
			public void evictedEntry(Object key, ValueWrapper value) {
				if (value.get() != null) {
					evictions.put(key, value.get());
				}
			}

		}, CacheUtils.getCleanupthread());

	}

	/**
	 * 构造方法 使用默认的cacheSize
	 */
	public ConcurrentLruHashMapCache() {
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
	protected Object fromStoreValue(Object storeValue) {
		return storeValue;
	}

	/**
	 * Convert the given user value, as passed into the put method,
	 * to a value in the internal store (adapting <code>null</code>).
	 * @param userValue the given user value
	 * @return the value to store
	 */
	protected ValueWrapper toStoreValue(Object userValue) {
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
