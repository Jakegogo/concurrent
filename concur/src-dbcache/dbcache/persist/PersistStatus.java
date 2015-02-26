package dbcache.persist;

/**
 * 实体更新状态
 * @author jake
 * @date 2014-7-31-下午8:17:56
 */
public enum PersistStatus {
	
	/**
	 * 瞬时态
	 */
	TRANSIENT,
	
	/**
	 * 持久态
	 */
	PERSIST,

	/**
	 * 已删除
	 */
	DELETED
	
}