package dbcache.service.impl;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import dbcache.conf.CacheConfig;
import dbcache.model.IEntity;
import dbcache.proxy.asm.AsmFactory;
import dbcache.proxy.util.ClassUtil;
import dbcache.service.Cache;
import dbcache.service.ConfigFactory;
import dbcache.service.DbCacheService;
import dbcache.service.DbPersistService;
import dbcache.service.IndexService;
import dbcache.support.spring.DefaultEntityMethodAspect;

/**
 * DbCached缓存模块配置服务实现
 * @author Jake
 * @date 2014年9月14日下午8:57:54
 */
@Component
public class ConfigFactoryImpl implements ConfigFactory {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConfigFactoryImpl.class);


	private static final String entityClassProperty = "clazz";

	private static final String cacheProperty = "cache";

	private static final String proxyClassProperty = "proxyClazz";

	private static final String indexServiceProperty = "indexService";

	private static final String dbPersistServiceProperty = "dbPersistService";


	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private DefaultEntityMethodAspect methodAspect;


	/**
	 * DbCacheService实例映射
	 */
	@SuppressWarnings("rawtypes")
	private Map<Class<?>, DbCacheService> dbCacheServiceBeanMap = new ConcurrentHashMap<Class<?>, DbCacheService>();

	/**
	 * 配置映射
	 */
	private Map<Class<?>, CacheConfig> cacheConfigMap = new ConcurrentHashMap<Class<?>, CacheConfig>();


	@SuppressWarnings({ "rawtypes" })
	public DbCacheService getDbCacheServiceBean(Class<? extends IEntity> clz) {

		DbCacheService service = this.dbCacheServiceBeanMap.get(clz);
		if(service != null) {
			return service;
		}


		try {

			CacheConfig cacheConfig = this.getCacheConfig(clz);

			//创建新的bean
			service = applicationContext.getAutowireCapableBeanFactory().createBean(DbCacheServiceImpl.class);

			//设置实体类
			Field clazzField = DbCacheServiceImpl.class.getDeclaredField(entityClassProperty);
			inject(service, clazzField, clz);


			//初始化代理类
			Class<?> proxyClazz = AsmFactory.getEnhancedClass(clz, methodAspect);
			Field proxyClazzField = DbCacheServiceImpl.class.getDeclaredField(proxyClassProperty);
			inject(service, proxyClazzField, proxyClazz);


			//设置持久化PersistType方式的dbPersistService
			Field dbPersistServiceField = DbCacheServiceImpl.class.getDeclaredField(dbPersistServiceProperty);
			DbPersistService dbPersistService = (DbPersistService) applicationContext.getBean(cacheConfig.getPersistType().getDbPersistServiceClass());
			inject(service, dbPersistServiceField, dbPersistService);


			//初始化缓存实例
			Field cacheField = DbCacheServiceImpl.class.getDeclaredField(cacheProperty);
			Class<?> cacheClass = cacheConfig.getCacheType().getCacheClass();
			Cache cache = (Cache) applicationContext.getAutowireCapableBeanFactory().createBean(cacheClass);
			int concurrencyLevel = cacheConfig.getConcurrencyLevel() == 0? Runtime.getRuntime().availableProcessors() : cacheConfig.getConcurrencyLevel();
			cache.init(cacheConfig.getEntitySize(), concurrencyLevel);
			inject(service, cacheField, cache);


			//修改IndexService的cache
			Field indexServiceField = DbCacheServiceImpl.class.getDeclaredField(indexServiceProperty);
			ReflectionUtils.makeAccessible(indexServiceField);
			Class<?> indexServiceClass = indexServiceField.get(service).getClass();
			IndexService indexService = (IndexService) applicationContext.getAutowireCapableBeanFactory().createBean(indexServiceClass);
			inject(service, indexServiceField, indexService);

			// 索引服务缓存设置
			Cache indexCache = cache;
			if(cacheConfig.getIndexSize() > 0) {
				indexCache = (Cache) applicationContext.getAutowireCapableBeanFactory().createBean(cacheClass);
				indexCache.init(cacheConfig.getIndexSize(), concurrencyLevel);
			}

			Field cacheField1 = indexService.getClass().getDeclaredField(cacheProperty);
			ReflectionUtils.makeAccessible(cacheField1);
			inject(indexService, cacheField1, indexCache);

			dbCacheServiceBeanMap.put(clz, service);

		} catch(Exception e) {
			e.printStackTrace();
		}

		return service;
	}




	/**
	 * 获取实体类的CacheConfig
	 * @param clz 实体类
	 * @return
	 */
	private CacheConfig getCacheConfig(Class<?> clz) {
		CacheConfig cacheConfig = cacheConfigMap.get(clz);
		if(cacheConfig == null) {
			cacheConfig = CacheConfig.valueOf(clz);
			cacheConfigMap.put(clz, cacheConfig);
		}
		return cacheConfig;
	}


	/**
	 * 注入属性
	 * @param bean bean
	 * @param field 属性
	 * @param val 值
	 */
	private void inject(Object bean, Field field, Object val) {
		ReflectionUtils.makeAccessible(field);
		try {
			field.set(bean, val);
		} catch (Exception e) {
			FormattingTuple message = MessageFormatter.format("属性[{}]注入失败", field);
			logger.debug(message.getMessage());
			throw new IllegalStateException(message.getMessage(), e);
		}
	}




	@SuppressWarnings("rawtypes")
	@Override
	public IEntity<?> createProxyEntity(IEntity<?> entity, Class<?> proxyClass, IndexService indexService) {
		return ClassUtil.getProxyEntity(proxyClass, entity, indexService);
	}



	@Override
	@SuppressWarnings("rawtypes")
	public void registerDbCacheServiceBean(Class<? extends IEntity> clz,
			DbCacheService dbCacheService) {
		dbCacheServiceBeanMap.put(clz, dbCacheService);
	}


}
