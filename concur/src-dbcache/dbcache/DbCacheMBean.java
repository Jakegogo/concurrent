package dbcache;

import java.util.Map;

/**
 * DbCacheService的JMX接口支持
 * @author Jake
 * @date 2014年9月14日下午9:51:08
 */
public interface DbCacheMBean {

	/**
	 * 获取DbCacheServiceBean信息
	 * @return
	 */
	Map<String, String> getDbCacheServiceBeanInfo();


	/**
	 * 获取缓存实体类配置
	 * @return
	 */
	Map<String, String> getCacheConfigInfo();


	/**
	 * 获取入库处理服务信息
	 * @return
	 */
	Map<String, Object> getDbPersistInfo();

}
