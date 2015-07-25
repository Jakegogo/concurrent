package dbcache.anno;

import dbcache.conf.ShardStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分表注解 TODO 未实现
 * @author Jake
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Shard {
	
	/**
	 * 分表策略类
	 * @return
	 */
	public Class<? extends ShardStrategy> value();
	
}
