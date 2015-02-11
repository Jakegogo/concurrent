package dbcache.annotation;

import dbcache.conf.shard.ShardStrategy;

/**
 * 分表注解
 * 
 * @author Jake
 *
 */
public @interface Shard {
	
	/**
	 * 分表策略类
	 * @return
	 */
	public Class<? extends ShardStrategy> value();
	
}
