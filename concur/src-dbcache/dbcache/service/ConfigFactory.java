package dbcache.service;

import dbcache.conf.CacheConfig;
import dbcache.model.IEntity;

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
