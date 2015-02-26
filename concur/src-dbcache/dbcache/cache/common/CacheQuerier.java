package dbcache.cache.common;

/**
 * 缓存初始查询器
 * Created by Jake on 2014/12/28.
 */
public interface CacheQuerier<R> {

    /**
     * 查询方法
     * @param keys 键参数列表
     * @return
     */
    public R query(Object... keys);

}
