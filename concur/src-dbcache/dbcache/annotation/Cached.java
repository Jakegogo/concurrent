package dbcache.annotation;

import dbcache.conf.CacheType;
import dbcache.conf.PersistType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h5>实体缓存配置注解</h5>
 * <br/>没有标注此注解的实体类则使用默认配置
 * <br/>实体持久化属性需使用基本类型或String,byte,byte[]等
 * <br/>启用DynamicUpdate后:
 * <br/>1,含有json格式的持久化属性对应瞬时态的Map,List等结构时需使用@ChangeFields("对应json串字段")注解标明所有会修改到json属性的方法
 * <br/>2,外部put/add瞬时态的属性的情况需要单独提供getMapForUpdate()方法用于修改,并注解@ChangeFields("对应json串字段")
 * @see dbcache.annotation.Index
 * @see dbcache.annotation.DynamicUpdate
 * @see dbcache.annotation.JsonConvert
 * @author Jake
 * @date 2014年9月13日下午1:38:22
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {


	/**
	 * 缓存类型,默认CacheType.LRU
	 * @return
	 */
	public CacheType cacheType() default CacheType.LRU;


	/**
	 * 持久化处理方式,默认PersistType.INTIME
	 * @return
	 */
	public PersistType persistType() default PersistType.INTIME;


	/**
	 * 实体缓存大小上限,默认值100000
	 * @return
	 */
	public int entitySize() default 100000;


	/**
	 * 索引缓存大小,不设置则用entityCache同一个缓存的entitySize
	 * @return
	 */
	public int indexSize() default 0;

	/**
	 * 并发线程数,默认为运行时CPU核数
	 * @return
	 */
	public int concurrencyLevel() default 0;

	/**
	 * 是否启用索引服务
	 * @return
	 */
	public boolean enableIndex() default false;

}
