package dbcache.model;

/**
 * 入库模式
 * @author jake
 * @date 2014-7-31-下午8:16:48
 */
public enum FlushMode {
	
	/**
	 * 即时入库
	 * (提交到线程池并立即异步执行)
	 */
	INTIME,
	
	/**
	 * 延时入库
	 * (检测时间为1s,延迟时间参考
	 * @see dbcache.service.impl.DbRuleServiceImpl.delayWaitTimmer)
	 */
	DELAY
	
}
