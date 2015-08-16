package dbcache.cache.common;

import dbcache.DbCacheService;
import dbcache.cache.CacheUnit;
import dbcache.cache.ValueWrapper;
import dbcache.cache.impl.ConcurrentLruHashMapCache;
import utils.collections.concurrent.ConcurrentHashMapV8;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通用缓存类
 * @param <R> 缓存内容类型
 */
public class CommonCache<R> {

    /**
     * 默认共享缓存
     */
    private static CacheUnit shareCacheUnit = new ConcurrentLruHashMapCache() {{
        this.init("DEFAULT_COMMON_CACHE", 10000, Runtime.getRuntime().availableProcessors());
    }};

    /**
     * 缓存名称
     */
    private String name;

    /**
     * 等待锁map {key:lock}
     */
    private final ConcurrentMap<Object, Lock> WAITING_LOCK_MAP = new ConcurrentHashMapV8<Object, Lock>();

    /**
     * 缓存单元
     */
    private CacheUnit cacheUnit;

    /**
     * DbCacheService
     */
    private DbCacheService dbCacheService;

    /**
     * 缓存查询器
     */
    private CacheLoader<R> cacheQuery;


    protected CommonCache() {
    }

    public CommonCache(CacheLoader<R> cacheQuery) {
        this.cacheQuery = cacheQuery;
    }

    public R get(Object... key) {

        String cachedKey = buildCacheKey(key);
        ValueWrapper cacheWrapper = getCacheUnit().get(cachedKey);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }

        if (this.cacheQuery == null) {
            return null;
        }

        Lock lock = new ReentrantLock();
        Lock prevLock = WAITING_LOCK_MAP.putIfAbsent(cachedKey, lock);
        lock = prevLock != null ? prevLock : lock;

        lock.lock();
        try {
            cacheWrapper = getCacheUnit().get(cachedKey);
            if(cacheWrapper != null) {
                return (R) cacheWrapper.get();
            }

            R value = this.cacheQuery.load(key);

            cacheWrapper = getCacheUnit().putIfAbsent(cachedKey, value);
            if(cacheWrapper != null) {
                return (R) cacheWrapper.get();
            }

        } finally {
            WAITING_LOCK_MAP.remove(cachedKey);
            lock.unlock();
        }

        return null;
    }

    private String buildCacheKey(Object... key) {
        StringBuilder keyBuilder = new StringBuilder();
        for (Object key1 : key) {
            keyBuilder.append(key1).append("_");
        }
        return keyBuilder.toString();
    }


    public R put(Object key, Object value) {
        ValueWrapper cacheWrapper = getCacheUnit().put(buildCacheKey(key), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R put(Object[] key, Object value) {
        ValueWrapper cacheWrapper = getCacheUnit().put(buildCacheKey(key), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R putIfAbsent(Object key, Object value) {
        this.get(key);
        ValueWrapper cacheWrapper = getCacheUnit().putIfAbsent(buildCacheKey(key), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R putIfAbsent(Object[] key, Object value) {
        this.get(key);
        ValueWrapper cacheWrapper = getCacheUnit().putIfAbsent(buildCacheKey(key), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R replace(Object key, Object oldValue, Object newValue) {
        ValueWrapper cacheWrapper = getCacheUnit().replace(buildCacheKey(key), oldValue, newValue);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R replace(Object[] key, Object oldValue, Object newValue) {
        ValueWrapper cacheWrapper = getCacheUnit().replace(buildCacheKey(key), oldValue, newValue);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R evict(Object... key) {
        ValueWrapper cacheWrapper = getCacheUnit().evict(buildCacheKey(key));
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public void clear() {
        getCacheUnit().clear();
    }

    public CacheUnit getCacheUnit() {
        if (this.cacheUnit != null) {
            return this.cacheUnit;
        }
        if (this.dbCacheService != null) {
            this.cacheUnit = this.dbCacheService.getCacheUnit();
            return this.cacheUnit;
        }
        this.cacheUnit = shareCacheUnit;
        return this.cacheUnit;
    }

    public void setCacheUnit(CacheUnit cacheUnit) {
        this.cacheUnit = cacheUnit;
    }

    public void setDbCacheService(DbCacheService dbCacheService) {
        this.dbCacheService = dbCacheService;
    }

    public String getName() {
        return name == null ? cacheUnit.getName() : name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
