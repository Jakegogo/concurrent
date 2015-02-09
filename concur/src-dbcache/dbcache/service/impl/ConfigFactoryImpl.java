package dbcache.service.impl;

import dbcache.conf.CacheConfig;
import dbcache.conf.CacheType;
import dbcache.conf.JsonConverter;
import dbcache.conf.PersistType;
import dbcache.key.IdGenerator;
import dbcache.model.CacheObject;
import dbcache.model.IEntity;
import dbcache.model.WeakCacheEntity;
import dbcache.model.WeakCacheObject;
import dbcache.service.*;
import dbcache.support.asm.*;
import dbcache.utils.IntegerCounter;
import dbcache.utils.ThreadUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import javax.annotation.PostConstruct;
import javax.management.*;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * DbCached缓存模块配置服务实现
 * @author Jake
 * @date 2014年9月14日下午8:57:54
 */
@Component
public class ConfigFactoryImpl implements ConfigFactory, DbCacheMBean {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConfigFactoryImpl.class);


	private static final String entityClassProperty = "clazz";

	private static final String cacheProperty = "cache";

	private static final String proxyCacheConfigProperty = "cacheConfig";

	private static final String indexServiceProperty = "indexService";

	private static final String dbPersistServiceProperty = "dbPersistService";

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private IndexMethodProxyAspect indexAspect;
	
	@Autowired
	private ModifiedFieldMethodAspect fieldChangeAspect;
	
	/**
	 * 数据库入库规则服务
	 */
	@Autowired
	private DbRuleService dbRuleService;

	/**
	 * 即时持久化服务
	 */
	@Autowired
	@Qualifier("inTimeDbPersistService")
	private DbPersistService intimeDbPersistService;

	/**
	 * 延迟持久化服务
	 */
	@Autowired
	@Qualifier("delayDbPersistService")
	private DbPersistService delayDbPersistService;


	/**
	 * DbCacheService实例映射
	 */
	@SuppressWarnings("rawtypes")
	private ConcurrentMap<Class<? extends IEntity>, DbCacheService> dbCacheServiceBeanMap = new ConcurrentHashMap<Class<? extends IEntity>, DbCacheService>();

	/**
	 * 配置映射
	 */
	private ConcurrentMap<Class<?>, CacheConfig<?>> cacheConfigMap = new ConcurrentHashMap<Class<?>, CacheConfig<?>>();

	/**
	 * 持久化服务
	 */
	private ConcurrentMap<PersistType, DbPersistService> persistServiceMap = new ConcurrentHashMap<PersistType, DbPersistService>();


	@SuppressWarnings({ "rawtypes" })
	@Override
	public DbCacheService getDbCacheServiceBean(Class<? extends IEntity> clz) {

		DbCacheService service = this.dbCacheServiceBeanMap.get(clz);
		if(service != null) {
			return service;
		}

		//创建对应实体的CacheService
		service = this.createCacheService(clz);
		dbCacheServiceBeanMap.putIfAbsent(clz, service);
		service = dbCacheServiceBeanMap.get(clz);

		return service;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DbCacheService createCacheService(Class<? extends IEntity> clz) {

		DbCacheService service = null;

		try {
			//获取缓存配置
			CacheConfig cacheConfig = this.getCacheConfig(clz);

			//创建新的bean
			service = applicationContext.getAutowireCapableBeanFactory().createBean(DbCacheServiceImpl.class);


			//设置实体类
			Field clazzField = DbCacheServiceImpl.class.getDeclaredField(entityClassProperty);
			inject(service, clazzField, clz);


			//初始化代理类
			AbstractAsmMethodProxyAspect aspector = this.createAspector(cacheConfig);
			
			EnhancedClassInfo<?> classInfo = EntityAsmFactory.getEntityEnhancedClassInfo(clz, aspector);
			cacheConfig.setProxyClazz(classInfo.getProxyClass());
			cacheConfig.setConstructorBuilder(classInfo.getConstructorBuilder());

			Field cacheConfigField = DbCacheServiceImpl.class.getDeclaredField(proxyCacheConfigProperty);
			inject(service, cacheConfigField, cacheConfig);


			//初始化缓存实例
			Field cacheField = DbCacheServiceImpl.class.getDeclaredField(cacheProperty);
			Class<?> cacheClass = cacheConfig.getCacheType().getCacheClass();
			Cache cache = (Cache) applicationContext.getAutowireCapableBeanFactory().createBean(cacheClass);
			int concurrencyLevel = cacheConfig.getConcurrencyLevel() == 0? Runtime.getRuntime().availableProcessors() : cacheConfig.getConcurrencyLevel();
			cache.init("ENTITY_CACHE_" + cacheClass.getSimpleName(), cacheConfig.getEntitySize(), concurrencyLevel);
			inject(service, cacheField, cache);


			//设置持久化PersistType方式的dbPersistService
			Field dbPersistServiceField = DbCacheServiceImpl.class.getDeclaredField(dbPersistServiceProperty);
			PersistType persistType = cacheConfig.getPersistType();
			DbPersistService dbPersistService = persistServiceMap.get(persistType);
			if(dbPersistService == null) {
				
				if (persistType.getBeanName() != null) {
					dbPersistService = (DbPersistService) applicationContext.getBean(persistType.getBeanName(), persistType.getDbPersistServiceClass());
				} else {
					dbPersistService = (DbPersistService) applicationContext.getBean(persistType.getDbPersistServiceClass());
				}
				
				persistServiceMap.putIfAbsent(persistType, dbPersistService);
				dbPersistService = persistServiceMap.get(persistType);
			}
			inject(service, dbPersistServiceField, dbPersistService);


			//修改IndexService的cache
			Field indexServiceField = DbCacheServiceImpl.class.getDeclaredField(indexServiceProperty);
			ReflectionUtils.makeAccessible(indexServiceField);
			Class<?> indexServiceClass = indexServiceField.get(service).getClass();
			DbIndexService indexService = (DbIndexService) applicationContext.getAutowireCapableBeanFactory().createBean(indexServiceClass);
			inject(service, indexServiceField, indexService);


			// 索引服务缓存设置
			Cache indexCache = (Cache) applicationContext.getAutowireCapableBeanFactory().createBean(cacheConfig.getIndexCacheClass());
			// 初始化索引缓存
			indexCache.init("INDEX_CACHE_" + cacheClass.getSimpleName(), cacheConfig.getIndexSize() > 0 ? cacheConfig.getIndexSize() : cacheConfig.getEntitySize(), concurrencyLevel);

			Field cacheField1 = indexService.getClass().getDeclaredField(cacheProperty);
			ReflectionUtils.makeAccessible(cacheField1);
			inject(indexService, cacheField1, indexCache);


			//修改IndexService的cacheConfig
			Field cacheConfigField1 = indexService.getClass().getDeclaredField(proxyCacheConfigProperty);
			inject(indexService, cacheConfigField1, cacheConfig);

			// 初始化Id生成器
			dbRuleService.initIdGenerators(clz, cacheConfig);


			// 初始化DbCache服务
			service.init();

		} catch(Exception e) {
			e.printStackTrace();
		}

		return service;
	}

	
	// 创建AsmMethodProxyAspect
	private AbstractAsmMethodProxyAspect createAspector(CacheConfig cacheConfig) {
		if (cacheConfig.isEnableIndex() && !cacheConfig.isEnableDynamicUpdate()) {
			return this.indexAspect;
		}
		if (cacheConfig.isEnableDynamicUpdate() && !cacheConfig.isEnableIndex()) {
			return this.fieldChangeAspect;
		}
		if (cacheConfig.isEnableIndex() && cacheConfig.isEnableDynamicUpdate()) {
			return new ChainedMethodProxyAspect(fieldChangeAspect, indexAspect);
		}
		return new AbstractAsmMethodProxyAspect() {};
	}


	@Override
	public void registerEntityIdGenerator(Class<?> clazz, IdGenerator<?> idGenerator) {
		//获取缓存配置
		CacheConfig<?> cacheConfig = this.getCacheConfig(clazz);
		if(cacheConfig == null) {
			throw new IllegalArgumentException("找不到类:" + clazz.getName() + "配置.");
		}

		cacheConfig.setDefaultIdGenerator(idGenerator);
	}


	@Override
	public void registerEntityIdGenerator(int serverId, Class<?> clazz, IdGenerator<?> idGenerator) {
		//获取缓存配置
		CacheConfig<?> cacheConfig = this.getCacheConfig(clazz);
		if(cacheConfig == null) {
			throw new IllegalArgumentException("找不到类:" + clazz.getName() + "配置.");
		}

		Map<Integer, IdGenerator<?>> classIdGeneratorMap = cacheConfig.getIdGenerators();
		if (idGenerator == null) {
			classIdGeneratorMap.remove(serverId);
		} else {
			classIdGeneratorMap.put(serverId, idGenerator);
		}
	}


	/**
	 * 获取实体类的CacheConfig
	 * @param clz 实体类
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CacheConfig<?> getCacheConfig(final Class<?> clz) {
		CacheConfig cacheConfig = cacheConfigMap.get(clz);

		//初始化CacheConfig配置
		if(cacheConfig == null) {
			cacheConfig = CacheConfig.valueOf(clz);

			final Map<String, ValueGetter<?>> indexes = new HashMap<String, ValueGetter<?>>();
			final Map<String, JsonConverter> jsonAutoConverters = new HashMap<String, JsonConverter>();

			final IntegerCounter fieldIndexCounter = new IntegerCounter();
			ReflectionUtils.doWithFields(clz, new FieldCallback() {
				public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
					// 忽略静态属性和临时属性
					if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) ||
							field.isAnnotationPresent(javax.persistence.Transient.class)) {
						return;
					}
					
					int fieldIndex = fieldIndexCounter.getAndIncrement();
					// 处理索引注解
					if (field.isAnnotationPresent(org.hibernate.annotations.Index.class) ||
							field.isAnnotationPresent(dbcache.annotation.Index.class)) {
						String indexName = null;
						org.hibernate.annotations.Index indexAno = field.getAnnotation(org.hibernate.annotations.Index.class);
						if(indexAno != null) {
							indexName = indexAno.name();
						} else {
							dbcache.annotation.Index indexAno1 = field.getAnnotation(dbcache.annotation.Index.class);
							indexName = indexAno1.name();
						}

						try {
							indexes.put(indexName, AsmAccessHelper.createFieldGetter(clz, field));
						} catch (Exception e) {
							logger.equals("获取实体配置出错:生成索引失败(" + clz.getName() + "." + field.getName() + ").");
							e.printStackTrace();
						}
					}

					// 处理Json转换注解
					if (field.isAnnotationPresent(dbcache.annotation.JsonConvert.class)) {
						dbcache.annotation.JsonConvert jsonConvert = field.getAnnotation(dbcache.annotation.JsonConvert.class);
						try {
							jsonAutoConverters.put(jsonConvert.value(), JsonConverter.valueof(clz, field, jsonConvert.value(), fieldIndex));
						} catch (Exception e) {
							logger.equals("获取实体配置出错:生成json属性自动转换失败(" + clz.getName() + "." + field.getName() + ").");
							e.printStackTrace();
						}
					}

				}
			});

			cacheConfig.setIndexes(indexes);
			cacheConfig.setJsonAutoConverters(jsonAutoConverters);
			cacheConfig.setFieldCount(clz.getDeclaredFields().length);

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
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T createProxyEntity(T entity, Class<? extends IEntity> proxyClass, DbIndexService indexService, CacheConfig<T> cacheConfig, AtomicIntegerArray modifiedFields) {
		// 判断是否启用索引服务
		if(cacheConfig == null || (!cacheConfig.isEnableIndex() && !cacheConfig.isEnableDynamicUpdate())) {
			return entity;
		}
		return (T) cacheConfig.getConstructorBuilder().getProxyEntity(proxyClass, entity, indexService, modifiedFields);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> WeakCacheEntity<T, ?> wrapEntity(T entity, Class<? extends IEntity> entityClazz, Cache cache, Object key, CacheConfig<T> cacheConfig) {
		// 判断是否开启弱引用
		if(cacheConfig == null || cacheConfig.getCacheType() != CacheType.WEEKMAP) {
			return null;
		}
		return WeakCacheEntity.valueOf(entity, cache.getReferencequeue(), key);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> CacheObject<T> createCacheObject(
			T entity, Class<? extends IEntity> entityClazz,
			DbIndexService<?> indexService, Object key, Cache cache, CacheConfig<T> cacheConfig) {

		// 启用动态更新
		AtomicIntegerArray modifiedFields = null;
		if (cacheConfig.isEnableDynamicUpdate()) {
			modifiedFields = new AtomicIntegerArray(cacheConfig.getFieldCount());
		}
		T proxyEntity = this.createProxyEntity(entity, cacheConfig.getProxyClazz(), indexService, cacheConfig, modifiedFields);

		// 弱引用方式
		if(cacheConfig.getCacheType() == CacheType.WEEKMAP) {
			entity = (T) this.wrapEntity(entity, entityClazz, cache, key, cacheConfig);
			proxyEntity = (T) this.wrapEntity(proxyEntity, entityClazz, cache, key, cacheConfig);
		}

		// 生成CacheObject
		if(cacheConfig.getCacheType() == CacheType.WEEKMAP) {
			return new WeakCacheObject<T, WeakCacheEntity<T,?>>(entity, entity.getId(), (Class<T>) entityClazz, proxyEntity, key, cacheConfig.getIndexList(), cacheConfig.getJsonAutoConverterList(), modifiedFields);
		} else {
			return new CacheObject<T>(entity, entity.getId(), (Class<T>) entityClazz, proxyEntity, cacheConfig.getIndexList(), cacheConfig.getJsonAutoConverterList(), modifiedFields);
		}

	}


	@Override
	@SuppressWarnings("rawtypes")
	public void registerDbCacheServiceBean(Class<? extends IEntity> clz,
			DbCacheService dbCacheService) {
		dbCacheServiceBeanMap.put(clz, dbCacheService);
	}

	/**
	 * 获取代理对象
	 * @param proxyClass 代理类
	 * @param entity 被代理实体
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private <T> T getProxyEntity(Class<?> proxyClass, T entity) {
		Class<?>[] paramTypes = { entity.getClass() };
		Object[] params = { entity };
		Constructor<?> con;
		try {
			con = proxyClass.getConstructor(paramTypes);
			return (T) con.newInstance(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	//-----------------------JMX接口---------------------


	/**
	 * 注册JMX服务
	 * @throws MalformedObjectNameException
	 * @throws NotCompliantMBeanException
	 * @throws MBeanRegistrationException
	 * @throws InstanceAlreadyExistsException
	 */
	@PostConstruct
	public void init() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		// Get the Platform MBean Server
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

	    // Construct the ObjectName for the MBean we will register
	    final ObjectName name = new ObjectName("dbcache.service:type=DbCacheMBean");

	    final StandardMBean mbean = new StandardMBean(this, DbCacheMBean.class);

	    // Register the Hello World MBean
	    mbs.registerMBean(mbean, name);
	}


	@Override
	@SuppressWarnings("rawtypes")
	public Map<String, String> getDbCacheServiceBeanInfo() {
		Map<String, String> infoMap = new HashMap<String, String>();
		for(Entry<Class<? extends IEntity>, DbCacheService> entry : dbCacheServiceBeanMap.entrySet()) {
			infoMap.put(entry.getKey().getName(), entry.getValue().toString());
		}
		return infoMap;
	}


	@Override
	public Map<String, String> getCacheConfigInfo() {
		Map<String, String> infoMap = new HashMap<String, String>();
		for(Entry<Class<?>, CacheConfig<?>> entry : cacheConfigMap.entrySet()) {
			infoMap.put(entry.getKey().getName(), entry.getValue().toString());
		}
		return infoMap;
	}


	@Override
	public Map<String, Object> getDbPersistInfo() {
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("intimeDbPersistService", ThreadUtils.dumpThreadPool(
				"intimeDbPersistServiceTheadPool",
				this.intimeDbPersistService.getThreadPool()));
		infoMap.put("delayDbPersistService", ThreadUtils.dumpThreadPool(
				"delayDbPersistServiceTheadPool",
				this.delayDbPersistService.getThreadPool()));
		return infoMap;
	}



}
