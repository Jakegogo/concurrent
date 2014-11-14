package dbcache.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dbcache.model.PersistAction;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.NamedThreadFactory;
import dbcache.utils.ThreadUtils;

/**
 * 即时入库实现
 * @author Jake
 * @date 2014年8月13日上午12:27:50
 */
@Component("inTimeDbPersistService")
public class InTimeDbPersistService implements DbPersistService {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(InTimeDbPersistService.class);

	/**
	 * 缺省入库线程池容量
	 */
	private static final int DEFAULT_DB_POOL_SIZE = Runtime.getRuntime().availableProcessors()/2 + 1;


	/**
	 * 入库线程池
	 */
	private ExecutorService DB_POOL_SERVICE;


	@Autowired
	private DbRuleService dbRuleService;



	@Override
	public void init() {

		// 初始化入库线程
		ThreadGroup threadGroup = new ThreadGroup("缓存模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "即时入库线程池");

		// 设置线程池大小
		int dbPoolSize = dbRuleService.getDbPoolSize();
		if(dbPoolSize <= 0) {
			dbPoolSize = DEFAULT_DB_POOL_SIZE;
		}

		// 初始化线程池
		DB_POOL_SERVICE = Executors.newFixedThreadPool(dbPoolSize, threadFactory);

	}


	@Override
	public void awaitTermination() {
		//关闭消费入库线程池
		ThreadUtils.shundownThreadPool(DB_POOL_SERVICE, false);
	}


	/**
	 * 获取入库线程池
	 * @return
	 */
	@Override
	public ExecutorService getThreadPool() {
		return this.DB_POOL_SERVICE;
	}


	@Override
	public void logHadNotPersistEntity() {

	}


	@Override
	public void handlerPersist(PersistAction persistAction) {

		try {
			DB_POOL_SERVICE.submit(persistAction);
		} catch (RejectedExecutionException ex) {
			logger.error("提交任务到更新队列被拒绝,使用同步处理:RejectedExecutionException");

			this.handleTask(persistAction);

		} catch (Exception ex) {
			logger.error("提交任务到更新队列产生异常", ex);
		}
	}

	/**
	 * 处理持久化操作
	 * @param persistAction 持久化操作
	 */
	private void handleTask(PersistAction persistAction) {
		persistAction.run();
	}


}
