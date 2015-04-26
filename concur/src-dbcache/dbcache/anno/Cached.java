package dbcache.anno;

import dbcache.conf.CacheType;
import dbcache.conf.PersistType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h5>实体缓存配置注解</h5>
 * <br/>没有标注此注解的实体类则使用默认配置
 * <br/>实体需要持久化的属性需使用基本类型或String,byte,byte[]等
 * <br/>使用DynamicUpdate后:
 * <br/>由于采用字节码增强实现监听属性变化的方式,外部put/add瞬时的如Map,List等结构时的属性,需要在update之前再次set一遍
 * @see dbcache.anno.Index
 * @see dbcache.anno.DynamicUpdate
 * @see JsonType
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
	 * 实体缓存大小上限,默认值10000
	 * @return
	 */
	public int entitySize() default 10000;


	/**
	 * 索引缓存大小,不设置则共用entityCache缓存(大小为entitySize)
	 * @return
	 */
	public int indexSize() default 0;

	/**
	 * 并发线程数,默认为运行时CPU核数(Runtime.getRuntime().availableProcessors())
	 * @return
	 */
	public int concurrencyLevel() default 16;

	/**
	 * 是否启用索引服务
	 * <br/>未开启索引服务时,dbcache.anno.Index将不会生效
	 * @return
	 */
	public boolean enableIndex() default false;

}
