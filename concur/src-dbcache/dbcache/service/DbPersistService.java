package dbcache.service;

import java.util.concurrent.ExecutorService;

import dbcache.model.PersistAction;

/**
 * 实体入库服务接口
 * @author Jake
 * @date 2014年8月13日上午12:24:45
 */
public interface DbPersistService {

	/**
	 * 处理持久化
	 * @param persistAction 持久化行为
	 */
	public void handlerPersist(PersistAction persistAction);

	/**
	 * 等待处理完毕
	 */
	public void awaitTermination();

	/**
	 * 打印出未入库对象
	 */
	void logHadNotPersistEntity();

	/**
	 * 获取入库线程池
	 * @return ExecutorService
	 */
	ExecutorService getThreadPool();

}
