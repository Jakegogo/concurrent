package dbcache.conf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存规则合集
 * @author Jake
 * @date 2014年8月13日上午1:15:55
 */
public class CacheRule {

	/**
	 * Key字符串缓存
	 */
	private static Map<Class<?>, Map<Serializable, String>> entityCacheKeyMap = new HashMap<Class<?>, Map<Serializable, String>>();


	/**
	 * 取得实体key
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 * @return String key
	 */
	public static String getEntityIdKey(Serializable id, Class<?> entityClazz) {
		Map<Serializable, String> entityKeyMap = entityCacheKeyMap.get(entityClazz);
		if(entityKeyMap == null) {
			entityKeyMap = new HashMap<Serializable, String>();
			entityCacheKeyMap.put(entityClazz, entityKeyMap);
		}
		String key = entityKeyMap.get(id);
		if(key != null) {
			return key;
		}
		key = new StringBuilder(entityClazz.getName()).append("_").append(id).toString();
		entityKeyMap.put(id, key);
		return key;
	}

}
