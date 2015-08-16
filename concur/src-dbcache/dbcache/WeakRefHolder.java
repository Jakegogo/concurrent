package dbcache;

import dbcache.index.IndexObject;

/**
 * 弱引用对象的持有者
 * Created by Jake on 2015/7/25.
 */
public class WeakRefHolder {

    /**
     * 持有缓存对象的引用
     */
    private CacheObject<?> cacheObject;

    public WeakRefHolder() {
    }

    public void rlinkCacheObject(CacheObject<?> cacheObject) {
        this.cacheObject = cacheObject;
    }

    /**
     * 移除索引对象引用
     * @param indexObject IndexObject<?>
     */
    public void removeIndexObject(IndexObject<?> indexObject) {
        this.cacheObject.removeIndexObject(indexObject);
    }

    /**
     * 添加索引对象引用
     * @param indexObject IndexObject<?>
     */
    public void addIndexObject(IndexObject<?> indexObject) {
        this.cacheObject.addIndexObject(indexObject);
    }

}
