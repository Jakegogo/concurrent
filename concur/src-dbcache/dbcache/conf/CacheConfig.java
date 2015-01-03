package dbcache.conf;

import dbcache.annotation.Cached;
import dbcache.annotation.EnableIndex;
import dbcache.service.impl.ConcurrentLinkedHashMapCache;
import dbcache.support.asm.ValueGetter;
import dbcache.support.jackson.ToStringJsonSerializer;
import dbcache.utils.AnnotationUtils;
import dbcache.utils.JsonUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.*;

/**
 * 缓存配置
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

	/** 索引服务缓存类 */
	private Class<?> indexCacheClass = ConcurrentLinkedHashMapCache.class;

	/** 索引信息  索引名 - 属性 */
	private Map<String, ValueGetter<T>> indexes = new HashMap<String, ValueGetter<T>>();
	/** 索引信息  List */
	private List<ValueGetter<T>> indexList = new ArrayList<ValueGetter<T>>();

	/** json属性自动转换信息 json串属性名 - 转换信息 */
	private Map<String, JsonConverter<T>> jsonAutoConverters = new HashMap<String, JsonConverter<T>>();
	/** json属性自动转换信息 List */
	private List<JsonConverter<T>> jsonAutoConverterList = new ArrayList<JsonConverter<T>>();

	/**
	 * 获取实例
	 * @param entityClass 实体类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> CacheConfig<T> valueOf(Class<T> entityClass) {
		Cached cachedAnno = entityClass.getAnnotation(Cached.class);
		if(cachedAnno != null) {
			CacheConfig<T> cacheConfig = (CacheConfig<T>) valueOf(cachedAnno);
			cacheConfig.setClazz(entityClass);
			if(entityClass.isAnnotationPresent(EnableIndex.class)) {
				cacheConfig.setEnableIndex(true);
			}
			return cacheConfig;
		}
		return (CacheConfig<T>) valueOf();
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

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getProxyClazz() {
		return proxyClazz;
	}

	public void setProxyClazz(Class<T> proxyClazz) {
		this.proxyClazz = proxyClazz;
	}

	public CacheType getCacheType() {
		return cacheType;
	}

	public void setCacheType(CacheType cacheType) {
		this.cacheType = cacheType;
	}

	public PersistType getPersistType() {
		return persistType;
	}

	public void setPersistType(PersistType persistType) {
		this.persistType = persistType;
	}

	public int getEntitySize() {
		return entitySize;
	}

	public void setEntitySize(int entitySize) {
		this.entitySize = entitySize;
	}

	public int getIndexSize() {
		return indexSize;
	}

	public void setIndexSize(int indexSize) {
		this.indexSize = indexSize;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public boolean isEnableIndex() {
		return enableIndex;
	}

	public void setEnableIndex(boolean enableIndex) {
		this.enableIndex = enableIndex;
	}

	@JsonSerialize(using = ToStringJsonSerializer.class)
	public Map<String, ValueGetter<T>> getIndexes() {
		return indexes;
	}

	public void setIndexes(Map<String, ValueGetter<T>> indexes) {
		this.indexes = indexes;

		List<ValueGetter<T>> indexList = new ArrayList<ValueGetter<T>>(indexes.size());
		indexList.addAll(indexes.values());
		this.indexList = Collections.unmodifiableList(indexList);
	}

	public Class<?> getIndexCacheClass() {
		return indexCacheClass;
	}

	public void setIndexCacheClass(Class<?> indexCacheClass) {
		this.indexCacheClass = indexCacheClass;
	}


	public Map<String, JsonConverter<T>> getJsonAutoConverters() {
		return jsonAutoConverters;
	}

	public void setJsonAutoConverters(Map<String, JsonConverter<T>> jsonAutoConverters) {
		this.jsonAutoConverters = jsonAutoConverters;

		List<JsonConverter<T>> jsonAutoConverterList = new ArrayList<JsonConverter<T>>(jsonAutoConverters.size());
		jsonAutoConverterList.addAll(jsonAutoConverters.values());
		this.jsonAutoConverterList = Collections.unmodifiableList(jsonAutoConverterList);
	}

	public List<ValueGetter<T>> getIndexList() {
		return indexList;
	}

	public List<JsonConverter<T>> getJsonAutoConverterList() {
		return jsonAutoConverterList;
	}
}
