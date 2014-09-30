package dbcache.service;

import dbcache.conf.CacheConfig;
import dbcache.model.CacheObject;
import dbcache.model.IEntity;
import dbcache.model.UpdateStatus;

/**
 * DbCached缓存模块配置服务接口
 * @author Jake
 * @date 2014年9月14日下午8:57:08
 */
public interface ConfigFactory {


	/**
	 * 创建实体代理对象
	 * @param entity 实体
	 * @param proxyClass 代理类
	 * @param indexService 索引服务
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public IEntity<?> createProxyEntity(IEntity<?> entity, Class<?> proxyClass, DbIndexService indexService);

	/**
	 * 包装实体(弱引用)
	 * @param entity 实体
	 * @param entityClazz 实体类
	 * @param cache 实体缓存容器
	 * @param key 索引键
	 * @return
	 */
	public IEntity<?> wrapEntity(IEntity<?> entity, Class<?> entityClazz, Cache cache, Object key);

	/**
	 * 创建缓存对象
	 * @param entity 实体
	 * @param entityClazz 实体类
	 * @param indexService 索引服务
	 * @param cache 实体缓存容器
	 * @param updateStatus 更新状态
	 * @return
	 */
	public CacheObject<?> createCacheObject(IEntity<?> entity, Class<?> entityClazz, DbIndexService<?> indexService, Object key, Cache cache, UpdateStatus updateStatus);

	/**
	 * 获取DbCacheServiceBean
	 * @param clz 实体类
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public DbCacheService getDbCacheServiceBean(Class<? extends IEntity> clz);


	/**
	 * 注册DbCacheServiceBean
	 * <br/> 必须在 postProcessAfterInitialization执行之前注册DbCacheServiceBean才能生效
	 * @param clz 实体类
	 * @param dbCacheService DbCacheServiceBean
	 */
	@SuppressWarnings("rawtypes")
	public void registerDbCacheServiceBean(Class<? extends IEntity> clz, DbCacheService dbCacheService);


	/**
	 * 获取缓存实体类配置
	 * @param clz 缓存实体类
	 * @return
	 */
	public CacheConfig getCacheConfig(Class<?> clz);


}
