package dbcache.conf;


/**
 * DbCached 配置常量约定
 */
public interface CfgConstants {

	/**
	 * 实体缓存最大容量
	 */
	String KEY_MAX_CAPACITY_OF_ENTITY_CACHE = "dbcache.entitycache.maxcapacity";

	/**
	 * 延时队列最大缓存长度
	 */
	String MAX_QUEUE_SIZE_BEFORE_PERSIST = "dbcache.entitycache.maxqueuesize";

	/**
	 * 通用缓存最大容量
	 */
	String KEY_MAX_CAPACITY_OF_COMMON_CACHE = "dbcache.commoncache.maxcapacity";

	/**
	 * 入库线程池容量
	 */
	String KEY_DB_POOL_CAPACITY = "dbcache.dbpool.capacity";

	/**
	 * 服务器ID标识集合(1~89999, 多个以","隔开)
	 */
	String KEY_SERVER_ID_SET = "dbcache.server.id.set";

	/**
	 * 延迟入库时间间隔(毫秒)
	 */
	String DELAY_WAITTIMMER = "dbcache.delay.timmer";

	/**
	 * 分隔符定义
	 */
	String SPLIT = ",";
}
