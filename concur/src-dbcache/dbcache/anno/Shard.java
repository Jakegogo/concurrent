package dbcache.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dbcache.conf.shard.ShardStrategy;

/**
 * 分表注解
 * 
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
