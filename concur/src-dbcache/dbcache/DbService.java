package dbcache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 通用DbCacheService,默认使用LRU回收策略
 * @see DbCacheService
 * Created by Jake on 2015/4/26.
 */
public interface DbService {


    /**
     * 根据主键id取得实体
     * <br/>根据业务需要,一般需要外部对实体进行加锁
     * @param clazz 实体类
     * @param id 主键id
     * @return 实体对象
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T get(Class<T> clazz, PK id);


    /**
     * 根据主键id列表取得实体列表
     * @param clazz 实体类
     * @param idList 主键id列表
     * @return 实体对象列表
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> List<T> listById(Class<T> clazz, Collection<PK> idList);


    /**
     * 提交新建实体到更新队列(根据配置自动随机服标识;即时入库)
     * @param entity 新建实体对象
     * @return 返回保存的实体对象(可能与entity不是同一个实例)
     * @throws IllegalArgumentException 如果主键id==null
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T submitCreate(T entity);


    /**
     * 提交实体修改任务到更新队列(默认即时入库)
     * @param entity 实体
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> void submitUpdate(T entity);


    /**
     * 提交实体删除任务到更新队列(即时入库)
     * @param entity 实体
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> void submitDelete(T entity);


    /**
     * 提交实体删除任务到更新队列(即时入库)
     * @param clazz 实体类
     * @param id 主键id
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> void submitDelete(Class<T> clazz, PK id);


    /**
     * 根据索引获取实体列表
     * <br/>内部已维护索引表
     * @see dbcache.index.DbIndexService <PK>
     * @param clazz 实体类
     * @param indexName 索引名
     * @param indexValue 索引值
     * @return
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> List<T> listByIndex(Class<T> clazz, String indexName, Object indexValue);


    /**
     * 根据索引获取实体Id列表
     * <br/>内部已维护索引表
     * @see dbcache.index.DbIndexService <PK>
     * @param clazz 实体类
     * @param indexName 索引名
     * @param indexValue 索引值
     * @return
     */
    <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> Collection<PK> listIdByIndex(Class<T> clazz, String indexName, Object indexValue);


}
