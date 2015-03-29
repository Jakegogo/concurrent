package dbcache.utils;

import dbcache.cache.common.CacheQuerier;
import dbcache.cache.CacheUnit;
import dbcache.DbCacheService;
import dbcache.cache.common.CommonCache;
import dbcache.cache.impl.ConcurrentLruHashMapCache;
import dbcache.utils.concurrent.CleanupThread;
import dbcache.utils.concurrent.ConcurrentLRUCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存工具类
 * 默认使用ConcurrentLruHashMapCache缓存单元
 * Created by Jake on 2014/12/28.
 */
public class CacheUtils {

	/**
	 * LRU Cache清除线程(单线程)
	 */
	private static final CleanupThread cleanupThread = new CleanupThread();

    /**
     * 所有通用缓存
     */
    private static final Map<String, CommonCache<?>> COMMON_CACHE_MAP = new ConcurrentHashMap<String, CommonCache<?>>();


    /**
     * 创建缓存容器
     * @param size 限制大小
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static ConcurrentLRUCache createLruCache(final int size) {
    	int flexibleSize = (size * 4 + 3) / 3;
    	return new ConcurrentLRUCache(flexibleSize, size, (int) Math
                .floor((size + flexibleSize) / 2), (int) Math
                .ceil(0.75f * flexibleSize), true, false, Runtime.getRuntime().availableProcessors(), null, cleanupThread);
    }

    /**
     * 创建缓存容器
     * @param size 限制大小
     * @param initialSize 初始大小
     * @param concurrencyLevel 并发数量
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static ConcurrentLRUCache createLruCache(final int size, final int initialSize, final int concurrencyLevel) {
    	int flexibleSize = (size * 4 + 3) / 3;
    	return new ConcurrentLRUCache(flexibleSize, size, (int) Math
                .floor((size + flexibleSize) / 2), initialSize, true, false, concurrencyLevel, null, cleanupThread);
    }

    /**
     * 使用Cache创建一个CacheBuilder
     * @param cacheQuery 缓存初始化
     * @return CacheBuilder
     */
    public static <R> CacheBuilder<R> cacheBuilder(CacheQuerier<R> cacheQuery) {
        return new CacheBuilder<R>(cacheQuery);
    }

    /**
     * 获取索引的通用缓存
     * @return Map<String, CommonCache>
     */
    public static Map<String, CommonCache<?>> getCommonCaches() {
        return COMMON_CACHE_MAP;
    }


    public static CleanupThread getCleanupthread() {
		return cleanupThread;
	}


	/**
     * 通用缓存构建器
     */
    public static class CacheBuilder<R> {

        /**
         * 通用缓存单元
         */
        private CommonCache commonCache;

        /**
         * 缓存查询器
         */
        private CacheQuerier<R> cacheQuery;

        /**
         * 构造方法
         * @param cacheQuery 缓存初始化查询
         */
        public CacheBuilder(CacheQuerier<R> cacheQuery) {
            this.cacheQuery = cacheQuery;
            this.commonCache = new CommonCache<R>(cacheQuery);
        }

        /**
         * 使用指定的缓存单元
         * @param cacheUnit 缓存单元
         * @return CacheBuilder<R>
         */
        public CacheBuilder<R> use(CacheUnit cacheUnit) {
            this.commonCache.setCacheUnit(cacheUnit);
            return this;
        }

        /**
         * 使用指定DbCacheService的缓存单元
         * @param dbCacheService DbCacheService的缓存单元
         * @return CacheBuilder<R>
         */
        public CacheBuilder<R> use(DbCacheService dbCacheService) {
            this.commonCache.setDbCacheService(dbCacheService);
            return this;
        }


        /**
         * 使用新的缓存单元
         * @param name 缓存名称
         * @param size 缓存大小
         * @return
         */
        public CacheBuilder<R> newCacheUnit(final String name, final int size) {
            this.commonCache.setName(name);
            this.commonCache.setCacheUnit(new ConcurrentLruHashMapCache() {{
                this.init(name, size, Runtime.getRuntime().availableProcessors());
            }});
            return this;
        }


        /**
         * 构建通用缓存
         * @return CommonCache<R>
         */
        public CommonCache<R> build() {

            if(commonCache.getCacheUnit() == null) {
                // TODO
            }

            COMMON_CACHE_MAP.put(commonCache.getName(), commonCache);
            return commonCache;
        }

    }


}
