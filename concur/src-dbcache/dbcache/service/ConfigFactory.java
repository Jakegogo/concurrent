package dbcache.service;

import dbcache.conf.CacheConfig;
import dbcache.key.IdGenerator;
import dbcache.model.CacheObject;
import dbcache.model.IEntity;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerArray;

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
	 * @param cacheConfig 缓存配置
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T createProxyEntity(T entity, Class<? extends IEntity> proxyClass, DbIndexService indexService, CacheConfig<T> cacheConfig, AtomicIntegerArray modifiedFields);


	/**
	 * 创建缓存对象
	 * @param entity 实体
	 * @param class1 实体类
	 * @param indexService 索引服务
	 * @param cache 实体缓存容器
	 * @param cacheConfig 缓存配置
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> CacheObject<T> createCacheObject(T entity, Class<? extends IEntity> class1, DbIndexService<?> indexService, Object key, Cache cache, CacheConfig<T> cacheConfig);


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
	 * 注册实体默认的主键id生成器
	 * @param clazz  实体类型
	 * @param idGenerator 主键id生成器接口
	 */
	public void registerEntityIdGenerator(Class<?> clazz, IdGenerator<?> idGenerator);


	/**
	 * 注册实体主键id生成器
	 * @param serverId 服标识
	 * @param clazz  实体类型
	 * @param idGenerator 主键id生成器接口
	 */
	public void registerEntityIdGenerator(int serverId, Class<?> clazz, IdGenerator<?> idGenerator);


	/**
	 * 获取缓存实体类配置
	 * @param clz 缓存实体类
	 * @return
	 */
	public CacheConfig<?> getCacheConfig(Class<?> clz);


}
