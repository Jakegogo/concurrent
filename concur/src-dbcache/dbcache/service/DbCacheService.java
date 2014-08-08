package dbcache.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dbcache.model.BaseModel;
import dbcache.model.FlushMode;


/**
 * 数据库缓存接口
 * @author jake
 * @date 2014-7-31-下午6:06:15
 */
public interface DbCacheService {
	
	
	/**
	 * 根据主键id取得实体
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 * @return 实体对象
	 */
	<T> T get(Serializable id, Class<T> entityClazz);
	
	
	/**
	 * 根据主键id列表取得实体列表
	 * @param idList 主键id列表
	 * @param entityClazz 实体类型
	 * @return 实体对象列表
	 */
	<T, PK extends Serializable> List<T> getEntityFromIdList(Collection<PK> idList, Class<T> entityClazz);
	
	
	/**
	 * 提交新建实体到更新队列(根据配置自动随机服标识;即时入库)
	 * @param entity 新建实体对象
	 * @return 返回保存的实体对象(可能与entity不是同一个实例)
	 * @throws IllegalArgumentException 如果主键id==null
	 */
	<T extends BaseModel<PK>, PK extends Comparable<PK> & Serializable> T submitNew2Queue(T entity);
	
	
	/**
	 * 提交实体修改任务到更新队列(默认即时入库)
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 */
	void submitUpdated2Queue(Serializable id, Class<?> entityClazz);
	
	
	/**
	 * 提交实体修改任务到更新队列
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 * @param flushMode 刷库模式
	 */
	void submitUpdated2Queue(Serializable id, Class<?> entityClazz, FlushMode flushMode);
	
	
	/**
	 * 提交实体删除任务到更新队列(即时入库)
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 */
	void submitDeleted2Queue(Serializable id, Class<?> entityClazz);
	
	
	/**
	 * 刷新所有延时入库的实体到库中
	 * 此方法为同步执行
	 */
	void flushAllEntity();
	
	
	/**
	 * 提交延时入库任务
	 * 此方法为异步执行
	 * TODO 定时执行
	 */
	void submitFlushTask();
	
	
	/**
	 * 关闭应用时回调
	 */
	void onCloseApplication();
	
	
	/**
	 * 获取入库线程池
	 * @return
	 */
	ExecutorService getThreadPool();
	
	
}
