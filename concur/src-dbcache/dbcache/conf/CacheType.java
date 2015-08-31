package dbcache.conf;

import dbcache.cache.impl.ConcurrentLinkedHashMapCache;
import dbcache.cache.impl.ConcurrentLruHashMapCache;
import dbcache.cache.impl.ConcurrentWeekHashMapCache;

/**
 * 缓存类型
 * @author Jake
 * @date 2014年9月13日下午1:54:23
 */
public enum CacheType {

	/**
	 * Apache ConcurrentLRUCache LRU
	 */
	LRU(ConcurrentLruHashMapCache.class),


	/**
	 * Google ConcurrentLinkedHashMap LRU
	 */
	LRU1(ConcurrentLinkedHashMapCache.class),


	/**
	 * 使用WeekHashMap
	 */
	WEEKMAP(ConcurrentWeekHashMapCache.class);

	/** 缓存类 */
	private final Class<?> cacheClass;

	CacheType(Class<?> cacheClass) {
		this.cacheClass = cacheClass;
	}

	public Class<?> getCacheClass() {
		return cacheClass;
	}


}
