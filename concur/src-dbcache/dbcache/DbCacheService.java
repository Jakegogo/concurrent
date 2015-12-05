package dbcache;

import dbcache.cache.CacheUnit;
import dbcache.index.DbIndexService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * 数据库缓存接口
 * <p>使用map缓存key-value形式的数据库存取方式,拥有较高的缓存命中率和缓存有效性。
 * 默认采用自有封装的jdbc,懒加载查询缓存，异步延迟入库保证最终数据准确性。
 * 集成了spring自动注入DBCache缓存服务实例，以实体类一服务，降低hash碰撞，实体缓存隔离。
 * 使用asm字节码生成实体代理，底层实现自动维护实体索引。</p>
 * <br/>集成了Cache,调用接口将同时修改缓存并同步到数据库
 * <br/>集成了DbIndexService,支持自动维护单列索引。需要修改数据索引,需启用dbcache.anno.Cached#enableIndex(),直接调用实体更改的方法即可
 * <br/>仅采用Hibernate的自动建表工具,数据库交互使用Jdbc,@see {@link dbcache.support.jdbc.JdbcSupport}
 * <br/>数据库交互不使用事务,需要业务逻辑维护缓存的事务性.
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
	 * @see dbcache.index.DbIndexService <PK>
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return
	 */
	List<T> listByIndex(String indexName, Object indexValue);


	/**
	 * 根据索引获取实体Id列表
	 * <br/>内部已维护索引表
	 * @see dbcache.index.DbIndexService <PK>
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return
	 */
	Collection<PK> listIdByIndex(String indexName, Object indexValue);


	/**
	 * 获取入库线程池
	 * @return ExecutorService
	 */
	ExecutorService getThreadPool();


	/**
	 * 获取缓存单元
	 * <br/>对缓存单元的操作将不会同步到数据库
	 * <br/>
	 * @return
	 */
	CacheUnit getCacheUnit();


	/**
	 * 获取索引Service
	 * <br/>对DbIndexService的操作将不会同步到数据库
	 * <br/>需要修改数据索引,需启用dbcache.anno.Cached#enableIndex(),直接调用实体更改的方法即可
	 * @return
	 */
	DbIndexService<PK> getIndexService();

}
