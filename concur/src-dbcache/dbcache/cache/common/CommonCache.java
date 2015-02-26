package dbcache.cache.common;

import dbcache.DbCacheService;
import dbcache.cache.CacheUnit;
import dbcache.cache.impl.ConcurrentLruHashMapCache;
import dbcache.utils.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
    private static CacheUnit defaultCacheUnit = new ConcurrentLruHashMapCache() {{
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
    private CacheUnit cacheUnit;

    /**
     * DbCacheService
     */
    private DbCacheService dbCacheService;

    /**
     * 缓存查询器
     */
    private CacheQuerier<R> cacheQuery;


    public CommonCache() {
    }

    public CommonCache(CacheQuerier<R> cacheQuery) {
        this.cacheQuery = cacheQuery;
    }

    public R get(Object... key) {
        Integer cachedKey = new HashCodeBuilder().append(this).append(key).toHashCode();
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().get(cachedKey);
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

            R value = this.cacheQuery.query(key);

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


    public R put(Object key, Object value) {
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().put(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R put(Object[] key, Object value) {
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().put(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R putIfAbsent(Object key, Object value) {
        this.get(key);
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().putIfAbsent(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R putIfAbsent(Object[] key, Object value) {
        this.get(key);
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().putIfAbsent(new HashCodeBuilder().append(this).append(key).toHashCode(), value);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R replace(Object key, Object oldValue, Object newValue) {
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().replace(new HashCodeBuilder().append(this).append(key).toHashCode(), oldValue, newValue);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R replace(Object[] key, Object oldValue, Object newValue) {
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().replace(new HashCodeBuilder().append(this).append(key).toHashCode(), oldValue, newValue);
        if(cacheWrapper != null) {
            return (R) cacheWrapper.get();
        }
        return null;
    }


    public R evict(Object... key) {
        CacheUnit.ValueWrapper cacheWrapper = getCacheUnit().evict(new HashCodeBuilder().append(this).append(key).toHashCode());
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
        this.cacheUnit = defaultCacheUnit;
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
