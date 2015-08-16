package dbcache;

/**
 * 实体加载监听接口
 * <br/>适用于spring bean
 * <br/>如:给实体初始化临时属性,并且要求线程安全(原子性操作)的情形
 * Created by Jake on 2015/4/17.
 */
public interface EntityLoadListener<T extends IEntity<?>> {

    /**
     * 加载时回调
     * <br/>在:
     * DbCacheService#get(key),
     * DbCacheService#submitCreate(entity)
     * 返回之前
     * @param entity
     */
    public void onEntityLoad(T entity);

}
