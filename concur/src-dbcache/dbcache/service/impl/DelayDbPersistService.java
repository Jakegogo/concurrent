package dbcache.service.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dbcache.model.PersistAction;
import dbcache.service.DbAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.NamedThreadFactory;


/**
 * 延时入库实现类
 * @author Jake
 * @date 2014年8月13日上午12:31:06
 */
@Component("delayDbPersistService")
public class DelayDbPersistService implements DbPersistService {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DelayDbPersistService.class);

	/**
	 * 缺省入库线程池容量
	 */
	private static final int DEFAULT_DB_POOL_SIZE = 2;

	/**
	 * 更改实体队列
	 */
	private final ConcurrentLinkedQueue<UpdateAction> updateQueue = new ConcurrentLinkedQueue<UpdateAction>();

	/**
	 * 当前延迟更新的动作
	 */
	private volatile UpdateAction currentDelayUpdateAction;

	/**
	 * 入库线程池
	 */
	private ExecutorService DB_POOL_SERVICE;


	@Autowired
	private DbRuleService dbRuleService;


	@Autowired
	private DbAccessService dbAccessService;

	/**
	 * 默认的持久化服务
	 */
	@Autowired
	@Qualifier("inTimeDbPersistService")
	private DbPersistService dbPersistService;


	/**
	 * 延迟更新操作
	 * @author Jake
	 * @date 2014年9月17日上午12:38:21
	 */
	static class UpdateAction {

		PersistAction persistAction;

		long createTime = System.currentTimeMillis();

		public UpdateAction(PersistAction persistAction) {
			this.persistAction = persistAction;
		}

		public static UpdateAction valueOf(PersistAction persistAction) {
			return new UpdateAction(persistAction);
		}

	}


	@Override
	public void init() {

		// 初始化入库线程
		ThreadGroup threadGroup = new ThreadGroup("缓存模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "延迟入库线程池");

		DB_POOL_SERVICE = Executors.newFixedThreadPool(DEFAULT_DB_POOL_SIZE, threadFactory);

		//初始化延时入库检测线程
		final long delayWaitTimmer = dbRuleService.getDelayWaitTimmer();//延迟入库时间(毫秒)

		final long delayCheckTimmer = 1000;//延迟入库队列检测时间间隔(毫秒)

		DB_POOL_SERVICE.submit(new Runnable() {

			@Override
			public void run() {

				//循环定时检测入库,失败自动进入重试
				while(true) {

					UpdateAction updateAction = updateQueue.poll();
					try {

						long timeDiff = 0l;
						do {
							if(updateAction == null) {
								//等待下一个检测时间
								Thread.sleep(delayCheckTimmer);
								//获取下一个操作元素
								updateAction = updateQueue.poll();
								continue;
							}

							timeDiff = System.currentTimeMillis() - updateAction.createTime;

							//未到延迟入库时间
							if(timeDiff < delayWaitTimmer) {
								currentDelayUpdateAction = updateAction;
								//等待
								Thread.sleep(delayWaitTimmer - timeDiff);
							}

							//执行入库
							PersistAction persistAction = updateAction.persistAction;
							if(persistAction.valid()) {
								dbPersistService.handlerPersist(persistAction);
							}

							//获取下一个操作元素
							updateAction = updateQueue.poll();

						} while(true);

					} catch(Exception e) {
						e.printStackTrace();

						if(updateAction != null && updateAction.persistAction != null) {
							logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!" + updateAction.persistAction.getPersistInfo(), e);
						}
						//等待下一个检测时间重试入库
						try {
							Thread.sleep(delayCheckTimmer);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

	}


	@Override
	public void handlerPersist(PersistAction persistAction) {
		updateQueue.add(UpdateAction.valueOf(persistAction));
	}


	/**
	 * 提交延时入库任务
	 */
	public void submitFlushTask() {
		Runnable task = this.createFlushTask();

		try {
			DB_POOL_SERVICE.submit(task);
		} catch (RejectedExecutionException ex) {
			logger.error("提交延时入库任务到更新队列产生异常", ex);

			this.flushAllEntity();

		} catch (Exception ex) {
			logger.error("提交延时入库任务到更新队列产生异常", ex);
		}
	}


	/**
	 * 创建延时入库任务
	 * @return Runnable
	 */
	private Runnable createFlushTask() {
		return new Runnable() {
			@Override
			public void run() {
				flushAllEntity();
			}
		};
	}


	@Override
	public void awaitTermination() {
		//刷新所有延时入库的实体到库中
		this.flushAllEntity();
	}


	/**
	 * 持久化所有实体
	 */
	public void flushAllEntity() {
		//入库延迟队列中的实体
		UpdateAction updateAction = this.updateQueue.poll();
		while (updateAction != null) {
			//执行入库
			updateAction.persistAction.run();
			updateAction = this.updateQueue.poll();
		}

		//入库正在延迟处理的实体
		if(currentDelayUpdateAction != null) {
			currentDelayUpdateAction.persistAction.run();
		}
	}



	@Override
	public void logHadNotPersistEntity() {
		UpdateAction updateAction = null;
		for (Iterator<UpdateAction> it = this.updateQueue.iterator(); it.hasNext();) {
			updateAction = it.next();
			String persistInfo = updateAction.persistAction.getPersistInfo();
			if(!StringUtils.isBlank(persistInfo)) {
				logger.error("检测到可能未入库对象! " + persistInfo);
			}
		}
	}


	@Override
	public ExecutorService getThreadPool() {
		return DB_POOL_SERVICE;
	}


}
