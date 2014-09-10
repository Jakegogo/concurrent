package dbcache.service.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dbcache.conf.CacheRule;
import dbcache.model.CacheObject;
import dbcache.model.EntityInitializer;
import dbcache.model.UpdateAction;
import dbcache.model.UpdateType;
import dbcache.service.Cache;
import dbcache.service.DbAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.JsonUtils;
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
	private static final int DEFAULT_DB_POOL_SIZE = 1;

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
	 * 缓存器
	 */
	private Cache cache;


	@Override
	public void init(Cache cache) {

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
					try {

						UpdateAction updateAction = updateQueue.poll();
						long timeDiff = 0l;
						CacheObject<?> cacheObj = null;

						do {

							if(updateAction == null) {
								//等待下一个检测时间
								Thread.sleep(delayCheckTimmer);
								//获取下一个操作元素
								updateAction = updateQueue.poll();
								continue;
							}
							cacheObj = updateAction.getCacheObject();
							//缓存对象在提交之后被修改过
							if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
								//获取下一个操作元素
								updateAction = updateQueue.poll();
								continue;
							}
							timeDiff = System.currentTimeMillis() - updateAction.getCreateTime();

							//未到延迟入库时间
							if(timeDiff < delayWaitTimmer) {
								currentDelayUpdateAction = updateAction;
								//等待
								Thread.sleep(delayWaitTimmer - timeDiff);
							}
							//缓存对象在提交之后被修改过
							if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
								//获取下一个操作元素
								updateAction = updateQueue.poll();
								continue;
							}

							//执行入库
							doSyncDb(updateAction);

							//获取下一个操作元素
							updateAction = updateQueue.poll();

						} while(true);
					} catch(Exception e) {
						e.printStackTrace();

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

		this.cache = cache;
	}


	/**
	 * 提交到更新队列
	 * @param updateAction
	 */
	private void addToUpdateQueue(UpdateAction updateAction) {
		this.updateQueue.add(updateAction);
	}


	@Override
	public void handlerPersist(UpdateAction updateAction) {
		addToUpdateQueue(updateAction);
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


	public void flushAllEntity() {
		//入库延迟队列中的实体
		UpdateAction updateAction = this.updateQueue.poll();
		while (updateAction != null) {
			//执行入库
			doSyncDb(updateAction);

			updateAction = this.updateQueue.poll();
		}
		//入库正在延迟处理的实体
		if(currentDelayUpdateAction != null) {
			doSyncDb(currentDelayUpdateAction);
		}
	}



	@Override
	public void logHadNotPersistEntity() {
		UpdateAction updateAction = null;
		CacheObject<?> cacheObject = null;
		Object entity = null;
		for (Iterator<UpdateAction> it = this.updateQueue.iterator(); it.hasNext();) {
			updateAction = it.next();
			cacheObject = updateAction.getCacheObject();
			//缓存对象在提交之后被修改过
			if(updateAction.getEditVersion() >= cacheObject.getEditVersion()) {
				entity = cacheObject.getEntity();
				logger.error("检测到可能未入库对象! " + entity.getClass().getName() + ":" + JsonUtils.object2JsonString(entity));
			}
		}
	}


	/**
	 * 同步到db
	 * @param cacheObj 实体缓存
	 */
	private void doSyncDb(final UpdateAction updateAction) {
		if (updateAction == null || updateAction.getCacheObject() == null) {
			return;
		}

		CacheObject<?> cacheObj = updateAction.getCacheObject();

		//缓存对象在提交之后被修改过
		if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
			return;
		}

		//比较并更新入库版本号
		if (!cacheObj.compareAndUpdateDbSync(updateAction)) {
			return;
		}

		Object entity = null;
		try {
			entity = cacheObj.getEntity();

			//持久化前操作
			if(entity instanceof EntityInitializer){
				EntityInitializer entityInitializer = (EntityInitializer) entity;
				entityInitializer.doBeforePersist();
			}

			//缓存对象在提交之后被入库过
			if(cacheObj.getDbVersion() > updateAction.getEditVersion()) {
				return;
			}

			//持久化
			if (updateAction.getUpdateType() == UpdateType.DELETE) {
				dbAccessService.delete(cacheObj.getEntity().getClass(), cacheObj.getId());
				String key = CacheRule.getEntityIdKey(cacheObj.getId(), cacheObj.getClass());
				cache.evict(key);
			} else if (updateAction.getUpdateType() == UpdateType.INSERT) {
				dbAccessService.save(cacheObj.getEntity());
			} else {
				dbAccessService.update(cacheObj.getEntity());
			}

		} catch (Exception ex) {
			logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!" + entity.getClass().getName() + ":" + JsonUtils.object2JsonString(entity), ex);
		}

	}

	@Override
	public ExecutorService getThreadPool() {
		return DB_POOL_SERVICE;
	}

}
