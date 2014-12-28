package dbcache.utils;

import dbcache.service.Cache;
import dbcache.service.DbCacheService;
import dbcache.service.impl.ConcurrentLruHashMapCache;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通用缓存类
 * @param <R> 缓存内容类型
 */
public class CommonCache<R> {

    /**
     * 默认缓存
     */
    private static Cache defaultCache = new ConcurrentLruHashMapCache() {{
        this.init("DEFAULT_COMMON_CACHE", 100000, Runtime.getRuntime().availableProcessors());
    }};

    /**
     * 缓存名称
     */
    private String name;

    /**
     * 等待锁map {key:lock}
     */
    private final ConcurrentMap<Integer, Lock> WAITING_LOCK_MAP = new ConcurrentHashMap<Integer, Lock>();

    /**
     * 缓存单元
     */
    private Cache cache;

    /**
     * DbCacheService
     */
    private DbCacheService dbCacheService;

    /**
     * 缓存查询器
     */
    private CacheQuerier<R> cacheQuery;


    protected CommonCache(CacheQuerier<R> cacheQuery) {
        this.cacheQuery = cacheQuery;
    }

    public R get(Object... key) {
        Integer cachedKey = new HashCodeBuilder().append(this).append(key).toHashCode();
        Cache.ValueWrapper cacheWrapper = getCache().get(cachedKey);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }

        Lock lock = new ReentrantLock();
        Lock prevLock = WAITING_LOCK_MAP.putIfAbsent(cachedKey, lock);
        lock = prevLock != null ? prevLock : lock;

        lock.lock();
        try {
            cacheWrapper = getCache().get(cachedKey);
            if(cacheWrapper != null) {
                return (R) cacheWrapper.get();
            }

            R value = this.cacheQuery.query(key);

            cacheWrapper = getCache().putIfAbsent(cachedKey, value);
            if(cacheWrapper != null) {
                return (R) cacheWrapper.get();
            }

        } finally {
            WAITING_LOCK_MAP.remove(cachedKey);
            lock.unlock();
        }

        return null;
    }


    public R put(Object key, Object value) {
        Cache.ValueWrapper cacheWrapper = getCache().put(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R put(Object[] key, Object value) {
        Cache.ValueWrapper cacheWrapper = getCache().put(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R putIfAbsent(Object key, Object value) {
        this.get(key);
        Cache.ValueWrapper cacheWrapper = getCache().putIfAbsent(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R putIfAbsent(Object[] key, Object value) {
        this.get(key);
        Cache.ValueWrapper cacheWrapper = getCache().putIfAbsent(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R replace(Object key, Object oldValue, Object newValue) {
        Cache.ValueWrapper cacheWrapper = getCache().replace(new HashCodeBuilder().append(this).append(key).toHashCode(), oldValue, newValue);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R replace(Object[] key, Object oldValue, Object newValue) {
        Cache.ValueWrapper cacheWrapper = getCache().replace(new HashCodeBuilder().append(this).append(key).toHashCode(), oldValue, newValue);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R evict(Object... key) {
        Cache.ValueWrapper cacheWrapper = getCache().evict(new HashCodeBuilder().append(this).append(key).toHashCode());
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public void clear() {
        getCache().clear();
    }

    public Cache getCache() {
        if (this.cache != null) {
            return this.cache;
        }
        if (this.dbCacheService != null) {
            this.cache = this.dbCacheService.getCache();
            return this.cache;
        }
        this.cache = defaultCache;
        return this.cache;
    }

    protected void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setDbCacheService(DbCacheService dbCacheService) {
        this.dbCacheService = dbCacheService;
    }

    public String getName() {
        return name == null ? cache.getName() : name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
