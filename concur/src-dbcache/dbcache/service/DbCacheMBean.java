package dbcache.service;

import java.util.Map;

/**
 * DbCache的JMX扩展
 * @author Jake
 * @date 2014年9月14日下午9:51:08
 */
public interface DbCacheMBean {

	/**
	 * 获取DbCacheServiceBean信息
	 * @param clz 实体类
	 * @return
	 */
	public Map<String, String> getDbCacheServiceBeanInfo();


	/**
	 * 获取缓存实体类配置
	 * @param clz 缓存实体类
	 * @return
	 */
	public Map<String, String> getCacheConfigInfo();


	/**
	 * 获取入库处理服务信息
	 * @return
	 */
	public Map<String, Object> getDbPersistInfo();

}
