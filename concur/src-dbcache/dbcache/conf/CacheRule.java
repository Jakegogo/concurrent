package dbcache.conf;

import java.io.Serializable;

/**
 * 缓存规则合集
 * @author Jake
 * @date 2014年8月13日上午1:15:55
 */
public class CacheRule {

	/**
	 * 取得实体key
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 * @return String key
	 */
	public static String getEntityIdKey(Serializable id, Class<?> entityClazz) {
		return new StringBuilder().append(entityClazz.getName())
									.append("_")
									.append(id).toString();
	}
	
}
