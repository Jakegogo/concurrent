package dbcache.conf.impl;

import dbcache.EntityLoadListener;
import dbcache.anno.Cached;
import dbcache.anno.DynamicUpdate;
import dbcache.anno.EnableIndex;
import dbcache.anno.Shard;
import dbcache.cache.impl.ConcurrentLinkedHashMapCache;
import dbcache.conf.CacheType;
import dbcache.conf.PersistType;
import dbcache.conf.ShardStrategy;
import dbcache.index.IndexChangeListener;
import dbcache.pkey.IdGenerator;
import dbcache.support.asm.ConstructorBuilder;
import utils.JsonUtils;
import utils.enhance.asm.ValueGetter;
import utils.reflect.AnnotationUtils;

import java.util.*;

/**
 * 缓存配置Bean
 * @author Jake
 * @date 2014年9月14日下午5:05:51
 */
public class CacheConfig<T> {

	/** 默认配置 */
	private static CacheConfig<?> defaultConfig;

	/** 实体类 */
	private Class<T> clazz;

	/** 实体静态代理类  */
	private Class<T> proxyClazz;

	/** 代理类方法构造器 */
	private ConstructorBuilder constructorBuilder;

	/** 属性个数 */
	private int fieldCount;

	/** 缓存容器类型 */
	private CacheType cacheType;

	/** 持久化处理方式 */
	private PersistType persistType;

	/** 实体缓存大小(上限) */
	private int entitySize;

	/** 索引缓存大小(上限) */
	private int indexSize;

	/** 并发线程数 */
	private int concurrencyLevel;

	/** 启用索引服务 默认true */
	private boolean enableIndex = false;

	/** 启用动态更新 */
	private boolean enableDynamicUpdate = false;

	/** 索引服务缓存类 */
	private Class<?> indexCacheClass = ConcurrentLinkedHashMapCache.class;

	/** 索引信息  索引名 - 属性 */
	private Map<String, ValueGetter<T>> indexes = new HashMap<String, ValueGetter<T>>();
	
	/**
	 * 实体主键ID生成map {类别ID : {实体类： 主键id生成器} }
	 * <br/>category - IdGenerator
	 */
	private Map<Integer, IdGenerator<?>> idGenerators = new IdentityHashMap<Integer, IdGenerator<?>> ();

	/**  默认主键id生成器  */
	private IdGenerator<?> defaultIdGenerator;
	
	/** 分表策略类,null为不分表 */
	private ShardStrategy shardStrategy;

	/** 实体加载监听器集合 */
	private List<EntityLoadListener> entityLoadEventListeners = new ArrayList<EntityLoadListener>();

	/** 是否存在实体加载监听类 */
	private boolean hasLoadListeners = false;

	/** 监听索引变化监听器集合 */
	private List<IndexChangeListener> indexChangeListener = new ArrayList<IndexChangeListener>();

	/** 是否存在索引变化监听类 */
	private boolean hasIndexListeners = false;

	/** 是否在移除时候同时删除缓存 */
	private boolean evictWhenDelete = false;


	/**
	 * 获取实例
	 * @param entityClass 实体类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> CacheConfig<T> valueOf(Class<T> entityClass) {
		Cached cachedAnno = entityClass.getAnnotation(Cached.class);
		
		CacheConfig<T> cacheConfig = null;
		if(cachedAnno != null) {
			cacheConfig = (CacheConfig<T>) valueOf(cachedAnno);
		} else {
			cacheConfig = (CacheConfig<T>) valueOf();
		}
		
		cacheConfig.setClazz(entityClass);
		
		if (entityClass.isAnnotationPresent(EnableIndex.class)) {
			cacheConfig.setEnableIndex(true);
		}
		
		if (entityClass.isAnnotationPresent(DynamicUpdate.class)) {
			cacheConfig.setEnableDynamicUpdate(true);
		}
		
		if (entityClass.isAnnotationPresent(Shard.class)) {
			Shard shardAnno = entityClass.getAnnotation(Shard.class);
			Class<? extends ShardStrategy> shardStrategrClass = shardAnno.value();
			try {
				cacheConfig.setShardStrategy(shardStrategrClass.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalArgumentException("分表策略无法初始化:" + shardStrategrClass.getName(), e);
			}
		}
		
		return cacheConfig;
	}

	/**
	 * 获取实例
	 * @param cachedAnno Cache注解
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static CacheConfig<?> valueOf(Cached cachedAnno) {
		CacheConfig<?> cacheConfig = new CacheConfig();
		cacheConfig.setCacheType(cachedAnno.cacheType());
		cacheConfig.setPersistType(cachedAnno.persistType());
		cacheConfig.setEntitySize(cachedAnno.entitySize());
		cacheConfig.setIndexSize(cachedAnno.indexSize());
		cacheConfig.setConcurrencyLevel(cachedAnno.concurrencyLevel());
		cacheConfig.setEnableIndex(cachedAnno.enableIndex());
		cacheConfig.setEvictWhenDelete(cachedAnno.evictWhenDelete());
		return cacheConfig;
	}


	/**
	 * 获取默认的CacheConfig
	 * @return
	 */
	public static CacheConfig<?> valueOf() {
		if(defaultConfig != null) {
			return defaultConfig;
		}
		Cached cachedAnno = AnnotationUtils.getDafault(Cached.class);
		CacheConfig<?> cacheConfig = new CacheConfig<Object>();
		cacheConfig.setCacheType(cachedAnno.cacheType());
		cacheConfig.setPersistType(cachedAnno.persistType());
		cacheConfig.setEntitySize(cachedAnno.entitySize());
		cacheConfig.setIndexSize(cachedAnno.indexSize());
		cacheConfig.setConcurrencyLevel(cachedAnno.concurrencyLevel());
		return defaultConfig = cacheConfig;
	}


	/**
	 * 生成自增长Id
	 * @return
	 */
	public Object getIdAutoGenerateValue() {
		return this.defaultIdGenerator.generateId();
	}


	/**
	 * 生成自增长Id
	 * @param category 服Id
	 * @return
	 */
	public Object getIdAutoGenerateValue(int category) {
		IdGenerator<?> idGenerator = idGenerators.get(category);
		if (idGenerator != null) {
			return idGenerator.generateId();
		}
		return null;
	}


	@Override
	public String toString() {
		return JsonUtils.object2JsonString(this);
	}

	public static CacheConfig<?> getDefaultConfig() {
		return defaultConfig;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	protected void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getProxyClazz() {
		return proxyClazz;
	}

	protected void setProxyClazz(Class<T> proxyClazz) {
		this.proxyClazz = proxyClazz;
	}

	public CacheType getCacheType() {
		return cacheType;
	}

	protected void setCacheType(CacheType cacheType) {
		this.cacheType = cacheType;
	}

	public PersistType getPersistType() {
		return persistType;
	}

	protected void setPersistType(PersistType persistType) {
		this.persistType = persistType;
	}

	public int getEntitySize() {
		return entitySize;
	}

	protected void setEntitySize(int entitySize) {
		this.entitySize = entitySize;
	}

	public int getIndexSize() {
		return indexSize;
	}

	protected void setIndexSize(int indexSize) {
		this.indexSize = indexSize;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	protected void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public boolean isEnableIndex() {
		return enableIndex;
	}

	protected void setEnableIndex(boolean enableIndex) {
		this.enableIndex = enableIndex;
	}

	public Map<String, ValueGetter<T>> getIndexes() {
		return indexes;
	}

	protected void setIndexes(Map<String, ValueGetter<T>> indexes) {
		this.indexes = indexes;
	}

	public Class<?> getIndexCacheClass() {
		return indexCacheClass;
	}

	protected void setIndexCacheClass(Class<?> indexCacheClass) {
		this.indexCacheClass = indexCacheClass;
	}

	public Map<Integer, IdGenerator<?>> getIdGenerators() {
		return idGenerators;
	}

	protected void setIdGenerators(Map<Integer, IdGenerator<?>> idGenerators) {
		this.idGenerators = idGenerators;
	}

	public IdGenerator<?> getDefaultIdGenerator() {
		return defaultIdGenerator;
	}

	protected void setDefaultIdGenerator(IdGenerator<?> defaultIdGenerator) {
		this.defaultIdGenerator = defaultIdGenerator;
	}

	public ConstructorBuilder getConstructorBuilder() {
		return constructorBuilder;
	}

	protected void setConstructorBuilder(ConstructorBuilder constructorBuilder) {
		this.constructorBuilder = constructorBuilder;
	}

	public boolean isEnableDynamicUpdate() {
		return enableDynamicUpdate;
	}

	protected void setEnableDynamicUpdate(boolean enableDynamicUpdate) {
		this.enableDynamicUpdate = enableDynamicUpdate;
	}

	public int getFieldCount() {
		return fieldCount;
	}

	protected void setFieldCount(int fieldCount) {
		this.fieldCount = fieldCount;
	}

	public ShardStrategy getShardStrategy() {
		return shardStrategy;
	}

	protected void setShardStrategy(ShardStrategy shardStrategy) {
		this.shardStrategy = shardStrategy;
	}

	public List<EntityLoadListener> getEntityLoadEventListeners() {
		return entityLoadEventListeners;
	}

	public boolean isHasLoadListeners() {
		return hasLoadListeners;
	}

	public void setHasLoadListeners(boolean hasLoadListeners) {
		this.hasLoadListeners = hasLoadListeners;
	}

	public boolean isHasIndexListeners() {
		return hasIndexListeners;
	}

	public void setHasIndexListeners(boolean hasIndexListeners) {
		this.hasIndexListeners = hasIndexListeners;
	}

	public List<IndexChangeListener> getIndexChangeListener() {
		return indexChangeListener;
	}

	public boolean isEvictWhenDelete() {
		return evictWhenDelete;
	}

	protected void setEvictWhenDelete(boolean evictWhenDelete) {
		this.evictWhenDelete = evictWhenDelete;
	}
}
