package dbcache.service;

import java.util.List;

import dbcache.key.IdGenerator;


/**
 * 数据库规则服务接口
 * @author jake
 * @date 2014-8-1-下午9:38:37
 */
public interface DbRuleService {

	
	/**
	 * 获取 入库线程池大小
	 * @return
	 */
	int getDbPoolSize();
	
	/**
	 * 获取 实体扫描包
	 * @return
	 */
	String getEntityPackages();
	
	/**
	 * 获取 实体缓存数量限制
	 * @return
	 */
	int getEntityCacheSize();
	
	/**
	 * 取得服标识ID列表
	 * @return List<Integer>
	 */
	List<Integer> getServerIdList();
	
	/**
	 * 判断当前服是否合服
	 * @return
	 */
	boolean isServerMerged();
	
	/**
	 * 判断当前服是否包含服Id
	 * @param serverId 服Id
	 * @return
	 */
	boolean containsServerId(int serverId);
	
	
	/**
	 * 注册实体主键id生成器
	 * @param serverId 服标识
	 * @param clazz  实体类型
	 * @param idGenerator 主键id生成器接口
	 */
	void registerEntityIdGenerator(int serverId, Class<?> clazz, IdGenerator<?> idGenerator);
	
	
	/**
	 * 根据实体类型自动生成主键id值
	 * @param clazz 实体类型
	 * @return 主键id值
	 */
	Object getIdAutoGenerateValue(Class<?> clazz);
	
	
	/**
	 * 根据实体类型自动生成主键id值
	 * @param serverId 服标识
	 * @param clazz 实体类型
	 * @return 主键id值
	 */
	Object getIdAutoGenerateValue(int serverId, Class<?> clazz);
	
	/**
	 * 获取延迟入库时间(毫秒)
	 * @return
	 */
	long getDelayWaitTimmer();
	
}
