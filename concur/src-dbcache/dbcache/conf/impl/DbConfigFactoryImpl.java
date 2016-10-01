package dbcache.conf.impl;

import dbcache.*;
import dbcache.cache.CacheUnit;
import dbcache.conf.CacheType;
import dbcache.conf.DbConfigFactory;
import dbcache.conf.DbRuleService;
import dbcache.conf.PersistType;
import dbcache.index.DbIndexService;
import dbcache.persist.service.DbPersistService;
import dbcache.pkey.IdGenerator;
import dbcache.support.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import utils.collections.concurrent.ConcurrentHashMapV8;
import utils.enhance.asm.AsmAccessHelper;
import utils.enhance.asm.ValueGetter;
import utils.reflect.ReflectionUtility;
import utils.thread.ThreadUtils;

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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DbCached缓存模块配置服务实现
 * @author Jake
 * @date 2014年9月14日下午8:57:54
 */
@Component
public class DbConfigFactoryImpl implements DbConfigFactory, DbCacheMBean {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DbConfigFactory.class);


	private static final String entityClassProperty = "clazz";

	private static final String cacheProperty = "cacheUnit";

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
	private final ConcurrentMap<Class<? extends IEntity>, DbCacheService> dbCacheServiceBeanMap = new ConcurrentHashMapV8<Class<? extends IEntity>, DbCacheService>();

	/**
	 * 配置映射
	 */
	private final ConcurrentMap<Class<?>, CacheConfig<?>> cacheConfigMap = new ConcurrentHashMapV8<Class<?>, CacheConfig<?>>();

	/**
	 * 持久化服务
	 */
	private final ConcurrentMap<PersistType, DbPersistService> persistServiceMap = new ConcurrentHashMapV8<PersistType, DbPersistService>();



	@SuppressWarnings({ "rawtypes" })
	@Override
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> DbCacheService<T, PK> getDbCacheServiceBean(Class<T> clz) {

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
	@Override
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> DbCacheService<T, PK> createCacheService(Class<T> clz) {

		try {
			//获取缓存配置
			CacheConfig cacheConfig = createCacheConfig(clz);
			cacheConfigMap.put(clz, cacheConfig);

			//创建新的bean
			DbCacheServiceImpl service = applicationContext.getAutowireCapableBeanFactory()
					.createBean(DbCacheServiceImpl.class);


			//设置实体类
			Field clazzField = DbCacheServiceImpl.class.getDeclaredField(entityClassProperty);
			ReflectionUtility.inject(service, clazzField, clz);



			//初始化代理类
			AbstractAsmMethodProxyAspect aspector = this.createAspector(cacheConfig);
			
			EnhancedClassInfo<?> classInfo = EntityAsmFactory.getEntityEnhancedClassInfo(clz, aspector);
			cacheConfig.setProxyClazz(classInfo.getProxyClass());
			cacheConfig.setConstructorBuilder(classInfo.getConstructorBuilder());

			Field cacheConfigField = DbCacheServiceImpl.class.getDeclaredField(proxyCacheConfigProperty);
			ReflectionUtility.inject(service, cacheConfigField, cacheConfig);



			//初始化缓存实例
			Field cacheField = DbCacheServiceImpl.class.getDeclaredField(cacheProperty);
			Class<?> cacheClass = cacheConfig.getCacheType().getCacheClass();
			CacheUnit cacheUnit = (CacheUnit) applicationContext.getAutowireCapableBeanFactory()
					.createBean(cacheClass);

			int concurrencyLevel = cacheConfig.getConcurrencyLevel() == 0?
					Runtime.getRuntime().availableProcessors() : cacheConfig.getConcurrencyLevel();
			cacheUnit.init("ENTITY_CACHE_" + cacheClass.getSimpleName(), cacheConfig.getEntitySize(), concurrencyLevel);
			ReflectionUtility.inject(service, cacheField, cacheUnit);



			//设置持久化PersistType方式的dbPersistService
			Field dbPersistServiceField = DbCacheServiceImpl.class.getDeclaredField(dbPersistServiceProperty);
			PersistType persistType = cacheConfig.getPersistType();
			DbPersistService dbPersistService = persistServiceMap.get(persistType);
			if(dbPersistService == null) {
				
				if (persistType.getBeanName() != null) {
					dbPersistService = (DbPersistService) applicationContext
							.getBean(persistType.getBeanName(), persistType.getDbPersistServiceClass());
				} else {
					dbPersistService = (DbPersistService) applicationContext
							.getBean(persistType.getDbPersistServiceClass());
				}
				
				persistServiceMap.putIfAbsent(persistType, dbPersistService);
				dbPersistService = persistServiceMap.get(persistType);
			}
			ReflectionUtility.inject(service, dbPersistServiceField, dbPersistService);



			//修改IndexService的cache
			Field indexServiceField = DbCacheServiceImpl.class.getDeclaredField(indexServiceProperty);
			ReflectionUtils.makeAccessible(indexServiceField);
			Class<?> indexServiceClass = indexServiceField.get(service).getClass();

			DbIndexService indexService = (DbIndexService) applicationContext.getAutowireCapableBeanFactory()
					.createBean(indexServiceClass);
			ReflectionUtility.inject(service, indexServiceField, indexService);



			// 索引服务缓存设置
			CacheUnit indexCacheUnit = (CacheUnit) applicationContext.getAutowireCapableBeanFactory()
					.createBean(cacheConfig.getIndexCacheClass());
			// 初始化索引缓存
			indexCacheUnit.init("INDEX_CACHE_" + cacheClass.getSimpleName(),
					cacheConfig.getIndexSize() > 0 ?
							cacheConfig.getIndexSize() : cacheConfig.getEntitySize(), concurrencyLevel);

			Field cacheField1 = indexService.getClass().getDeclaredField(cacheProperty);
			ReflectionUtils.makeAccessible(cacheField1);
			ReflectionUtility.inject(indexService, cacheField1, indexCacheUnit);



			//修改IndexService的cacheConfig
			Field cacheConfigField1 = indexService.getClass().getDeclaredField(proxyCacheConfigProperty);
			ReflectionUtility.inject(indexService, cacheConfigField1, cacheConfig);



			// 初始化Id生成器
			dbRuleService.initIdGenerators(clz, cacheConfig);
			// 初始化DbCache服务
			service.init();

			return service;
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
			throw new DbCacheInitError("初始化实体DbCacheService异常" ,e);
		} catch(IllegalAccessException e) {
			e.printStackTrace();
			throw new DbCacheInitError("初始化实体DbCacheService异常" ,e);
		}

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
		cacheConfig.setDefaultIdGenerator(idGenerator);
	}


	@Override
	public void registerEntityIdGenerator(int serverId, Class<?> clazz, IdGenerator<?> idGenerator) {
		//获取缓存配置
		CacheConfig<?> cacheConfig = this.getCacheConfig(clazz);

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
			cacheConfig = createCacheConfig(clz);
			cacheConfigMap.put(clz, cacheConfig);
		}
		return cacheConfig;
	}


	// 构建CacheConfig Bean
	private CacheConfig createCacheConfig(final Class<?> clz) {

		CacheConfig cacheConfig = CacheConfig.valueOf(clz);
		final Map<String, ValueGetter<?>> indexes = new HashMap<String, ValueGetter<?>>();

		// 解析注解
		ReflectionUtils.doWithFields(clz, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				// 忽略静态属性和临时属性
				if (Modifier.isTransient(field.getModifiers())
						|| Modifier.isStatic(field.getModifiers())
						|| field.isAnnotationPresent(javax.persistence.Transient.class)) {
					return;
				}

				// 处理索引注解
				if (field.isAnnotationPresent(org.hibernate.annotations.Index.class) ||
						field.isAnnotationPresent(dbcache.anno.Index.class)) {

					org.hibernate.annotations.Index indexAno =
							field.getAnnotation(org.hibernate.annotations.Index.class);

					String indexName;
					if (indexAno != null) {
						indexName = indexAno.name();
					} else {
						dbcache.anno.Index indexAno1 = field.getAnnotation(dbcache.anno.Index.class);
						indexName = indexAno1.name();
					}

					try {
						indexes.put(indexName, AsmAccessHelper.createFieldGetter(field.getName(), clz, field));
					} catch (Exception e) {
						logger.error("获取实体配置出错:生成索引失败(" +
								clz.getName() + "." + field.getName() + ").");
						e.printStackTrace();
					}
				}

			}
		});

		cacheConfig.setIndexes(indexes);
		cacheConfig.setFieldCount(clz.getDeclaredFields().length);
		return cacheConfig;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T createProxyEntity(
			T entity,
			Class<? extends IEntity> proxyClass,
			DbIndexService indexService,
			CacheConfig<T> cacheConfig,
			AtomicIntegerArray modifiedFields) {
		// 判断是否启用索引服务
		if(cacheConfig == null || (!cacheConfig.isEnableIndex() && !cacheConfig.isEnableDynamicUpdate())) {
			return entity;
		}
		return (T) cacheConfig.getConstructorBuilder()
				.getProxyEntity(proxyClass, entity, indexService, modifiedFields);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> WeakCacheEntity<T, ?> wrapEntity(
			T entity,
			Class<? extends IEntity> entityClazz,
			CacheUnit cacheUnit,
			Object key,
			CacheConfig<T> cacheConfig) {
		// 判断是否开启弱引用
		if(cacheConfig == null || cacheConfig.getCacheType() != CacheType.WEEKMAP) {
			return null;
		}
		return WeakCacheEntity.valueOf(entity, cacheUnit.getReferencequeue(), key);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> CacheObject<T>
		createCacheObject(
			T entity, Class<T> entityClazz,
			DbIndexService<?> indexService,
			Object key,
			CacheUnit cacheUnit,
			CacheConfig<T> cacheConfig) {

		// 启用动态更新
		AtomicIntegerArray modifiedFields = null;
		if (cacheConfig.isEnableDynamicUpdate()) {
			modifiedFields = new AtomicIntegerArray(cacheConfig.getFieldCount());
		}

		T proxyEntity = this.createProxyEntity(
				entity,
				cacheConfig.getProxyClazz(),
				indexService,
				cacheConfig,
				modifiedFields);

		// 弱引用方式
		if(cacheConfig.getCacheType() == CacheType.WEEKMAP) {
			entity = (T) this.wrapEntity(
					entity,
					entityClazz,
					cacheUnit,
					key,
					cacheConfig);
			proxyEntity = (T) this.wrapEntity(
					proxyEntity,
					entityClazz,
					cacheUnit,
					key,
					cacheConfig);
		}

		// 生成CacheObject
		CacheObject<T> result;
		if(cacheConfig.getCacheType() == CacheType.WEEKMAP) {
			result = new WeakCacheObject<T, WeakCacheEntity<T,?>>(
					entity,
					entityClazz,
					proxyEntity,
					key,
					modifiedFields);
		} else {
			result = new CacheObject<T>(
					entity,
					entityClazz,
					proxyEntity,
					modifiedFields);
		}
		
		// 持久化前回调
		result.doBeforePersist(cacheConfig);

		// 设置引用持有对象
		if (proxyEntity instanceof EnhancedEntity) {
			((EnhancedEntity)proxyEntity).getRefHolder().rlinkCacheObject(result);
		}
		
		return result;
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


	private void clear() {
		dbCacheServiceBeanMap.clear();
		cacheConfigMap.clear();
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
	public void init() throws
			MalformedObjectNameException,
			InstanceAlreadyExistsException,
			MBeanRegistrationException,
			NotCompliantMBeanException {
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
