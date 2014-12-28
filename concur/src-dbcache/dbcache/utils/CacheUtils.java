package dbcache.utils;

import dbcache.service.Cache;
import dbcache.service.DbCacheService;
import dbcache.service.impl.ConcurrentLruHashMapCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存工具类
 * 默认使用ConcurrentLruHashMapCache缓存单元
 * Created by Jake on 2014/12/28.
 */
public class CacheUtils {


    /**
     * 所有通用缓存
     */
    private static Map<String, CommonCache> COMMON_CACHE_MAP = new ConcurrentHashMap<String, CommonCache>();

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
    public static Map<String, CommonCache> getCommonCaches() {
        return COMMON_CACHE_MAP;
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
         * @param cache 缓存单元
         * @return CacheBuilder<R>
         */
        public CacheBuilder<R> use(Cache cache) {
            this.commonCache.setCache(cache);
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
            this.commonCache.setCache(new ConcurrentLruHashMapCache() {{
                this.init(name, size, Runtime.getRuntime().availableProcessors());
            }});
            return this;
        }


        /**
         * 构建通用缓存
         * @return CommonCache<R>
         */
        public CommonCache<R> build() {

            if(commonCache.getCache() == null) {
                // TODO
            }

            COMMON_CACHE_MAP.put(commonCache.getName(), commonCache);
            return commonCache;
        }

    }


}
