package dbcache.conf;

import dbcache.annotation.Cached;
import dbcache.annotation.EnableIndex;
import dbcache.utils.AnnotationUtils;
import dbcache.utils.JsonUtils;

/**
 * 缓存配置
 * @author Jake
 * @date 2014年9月14日下午5:05:51
 */
public class CacheConfig {

	/** 默认配置 */
	private static CacheConfig defaultConfig;

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

	/**
	 * 获取实例
	 * @param entityClass 实体类
	 * @return
	 */
	public static CacheConfig valueOf(Class<?> entityClass) {
		Cached cachedAnno = entityClass.getAnnotation(Cached.class);
		if(cachedAnno != null) {
			CacheConfig cacheConfig = valueOf(cachedAnno);
			if(entityClass.isAnnotationPresent(EnableIndex.class)) {
				cacheConfig.setEnableIndex(true);
			}
			return cacheConfig;
		}
		return valueOf();
	}

	/**
	 * 获取实例
	 * @param cachedAnno Cache注解
	 * @return
	 */
	public static CacheConfig valueOf(Cached cachedAnno) {
		CacheConfig cacheConfig = new CacheConfig();
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
	public static CacheConfig valueOf() {
		if(defaultConfig != null) {
			return defaultConfig;
		}
		Cached cachedAnno = AnnotationUtils.getDafault(Cached.class);
		CacheConfig cacheConfig = new CacheConfig();
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

	public static CacheConfig getDefaultConfig() {
		return defaultConfig;
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



}
