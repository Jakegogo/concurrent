package dbcache.annotation;

import dbcache.model.CacheType;

/**
 * 缓存配置注解
 * @author Jake
 * @date 2014年9月13日下午1:38:22
 */
public @interface CacheConfig {


	/**
	 * 缓存类型,不指定则试用默认值LRU
	 * @return
	 */
	public int cacheType() default CacheType.LRU;


	/**
	 * 实体缓存大小,不指定则试用默认值100000
	 * @return
	 */
	public int entitySize() default 0;


	/**
	 * 索引缓存大小,不设置则用entityCache同一个缓存的entitySize
	 * @return
	 */
	public int indexSize() default 0;


}
