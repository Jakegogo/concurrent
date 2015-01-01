package dbcache.service;

import dbcache.model.CacheObject;

import java.util.concurrent.ExecutorService;

/**
 * 实体入库服务接口
 * @author Jake
 * @date 2014年8月13日上午12:24:45
 */
public interface DbPersistService {

	/**
	 * 处理创建
	 * @param cacheObject 实体缓存对象
	 * @param dbAccessService 数据库存取服务
	 */
	public void handleSave(CacheObject<?> cacheObject, DbAccessService dbAccessService);

	/**
	 * 处理更新
	 * @param cacheObject 实体缓存对象
	 * @param dbAccessService 数据库存取服务
	 */
	public void handleUpdate(CacheObject<?> cacheObject, DbAccessService dbAccessService);

	/**
	 * 处理删除
	 * @param cacheObject 实体缓存对象
	 * @param dbAccessService 数据库存取服务
	 * @param key 缓存key
	 * @param cache 缓存容器
	 */
	public void handleDelete(CacheObject<?> cacheObject, DbAccessService dbAccessService, Object key, Cache cache);

	/**
	 * 等待处理完毕
	 */
	public void awaitTermination();

	/**
	 * 打印出未入库对象
	 */
	void logHadNotPersistEntity();

	/**
	 * 获取入库线程池
	 * @return ExecutorService
	 */
	ExecutorService getThreadPool();

}
