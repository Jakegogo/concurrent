package dbcache.cache.common;

/**
 * 缓存加载接口
 * Created by Jake on 2014/12/28.
 */
public interface CacheLoader<R> {

    /**
     * 加载方法
     * @param keys 键参数列表
     * @return
     */
    R load(Object... keys);

}
