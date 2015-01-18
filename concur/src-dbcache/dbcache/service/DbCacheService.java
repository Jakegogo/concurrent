package dbcache.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dbcache.key.IdGenerator;
import dbcache.model.IEntity;


/**
 * 数据库缓存接口
 * @author jake
 * @date 2014-7-31-下午6:06:15
 */
public interface DbCacheService<T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> {


	/**
	 * 根据主键id取得实体
	 * <br/>根据业务需要,一般需要外部对实体进行加锁
	 * @param id 主键id
	 * @return 实体对象
	 */
	T get(PK id);


	/**
	 * 根据主键id列表取得实体列表
	 * @param idList 主键id列表
	 * @return 实体对象列表
	 */
	List<T> listById(Collection<PK> idList);


	/**
	 * 提交新建实体到更新队列(根据配置自动随机服标识;即时入库)
	 * @param entity 新建实体对象
	 * @return 返回保存的实体对象(可能与entity不是同一个实例)
	 * @throws IllegalArgumentException 如果主键id==null
	 */
	T submitCreate(T entity);


	/**
	 * 提交实体修改任务到更新队列(默认即时入库)
	 * @param entity 实体
	 */
	void submitUpdate(T entity);


	/**
	 * 提交实体删除任务到更新队列(即时入库)
	 * @param entity 实体
	 */
	void submitDelete(T entity);


	/**
	 * 提交实体删除任务到更新队列(即时入库)
	 * @param id 主键id
	 */
	void submitDelete(PK id);


	/**
	 * 根据索引获取实体列表
	 * <br/>内部已维护索引表
	 * @see dbcache.service.IndexService<PK>
	 * @see dbcache.model.Sortable<PK>
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return
	 */
	List<T> listByIndex(String indexName, Object indexValue);


	/**
	 * 根据索引获取实体Id列表
	 * <br/>内部已维护索引表
	 * @see dbcache.service.IndexService<PK>
	 * @see dbcache.model.Sortable<PK>
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return
	 */
	Collection<PK> listIdByIndex(String indexName, Object indexValue);


	/**
	 * 根据索引获取实体列表
	 * <br/>内部已维护索引表
	 * @see dbcache.service.IndexService<PK>
	 * @see dbcache.model.Sortable<PK>
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @param page 页码
	 * @param size 大小
	 * @return
	 */
	List<T> pageByIndex(String indexName, Object indexValue, int page, int size);


	/**
	 * DbCache初始化
	 */
	public void init();


	/**
	 * 关闭应用时回调
	 */
	void onCloseApplication();


	/**
	 * 获取入库线程池
	 * @return ExecutorService
	 */
	ExecutorService getThreadPool();


	/**
	 * 获取缓存
	 * @return
	 */
	Cache getCache();


	/**
	 * 获取索引Service
	 * @return
	 */
	DbIndexService<PK> getIndexService();


	/**
	 * 注册实体默认的主键id生成器
	 * @param idGenerator 主键id生成器接口
	 */
	void registerEntityIdGenerator(IdGenerator<?> idGenerator);


	/**
	 * 注册实体主键id生成器
	 * @param serverId 服标识
	 * @param idGenerator 主键id生成器接口
	 */
	void registerEntityIdGenerator(int serverId, IdGenerator<?> idGenerator);

}
