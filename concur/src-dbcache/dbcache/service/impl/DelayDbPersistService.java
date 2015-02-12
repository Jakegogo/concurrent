package dbcache.service.impl;

import dbcache.conf.CacheConfig;
import dbcache.model.CacheObject;
import dbcache.model.IEntity;
import dbcache.model.PersistAction;
import dbcache.model.PersistStatus;
import dbcache.service.CacheUnit;
import dbcache.service.DbAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.JsonUtils;
import dbcache.utils.NamedThreadFactory;
import dbcache.utils.ThreadUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.record.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 延时入库实现类
 * <br/>单线程执行入库
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
	 * 更改实体队列
	 */
	private final ConcurrentLinkedQueue<QueuedAction> updateQueue = new ConcurrentLinkedQueue<QueuedAction>();

	/**
	 * 当前延迟更新的动作
	 */
	private volatile QueuedAction currentDelayUpdateAction;


	@Autowired
	private DbRuleService dbRuleService;


	/**
	 * 入库线程池
	 */
	private ExecutorService DB_POOL_SERVICE;


	/**
	 * 延迟更新操作
	 * @author Jake
	 * @date 2014年9月17日上午12:38:21
	 */
	static class QueuedAction {

		PersistAction persistAction;

		long createTime = System.currentTimeMillis();

		public QueuedAction(PersistAction persistAction) {
			this.persistAction = persistAction;
		}

		public static QueuedAction valueOf(PersistAction persistAction) {
			return new QueuedAction(persistAction);
		}

		public void doRunTask() {
			if (persistAction.valid()) {
				persistAction.run();
			}
		}

	}


	@PostConstruct
	public void init() {

		//初始化延时入库检测线程
		final long delayWaitTimmer = dbRuleService.getDelayWaitTimmer();//延迟入库时间(毫秒)

		final long delayCheckTimmer = 1000;//延迟入库队列检测时间间隔(毫秒)

		// 初始化入库线程
		ThreadGroup threadGroup = new ThreadGroup("缓存模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "延时入库线程池");
		DB_POOL_SERVICE = Executors.newSingleThreadExecutor(threadFactory);

		// 初始化入库线程
		DB_POOL_SERVICE.submit(new Runnable() {

			@Override
			public void run() {

				//循环定时检测入库,失败自动进入重试
				QueuedAction updateAction = updateQueue.poll();
				while (!Thread.interrupted()) {

					try {
						long timeDiff = 0l;
						do {

							if (updateAction == null) {
								
								//等待下一个检测时间
								Thread.sleep(delayCheckTimmer);
								
							} else if (updateAction.persistAction.valid()) {
								
								timeDiff = System.currentTimeMillis() - updateAction.createTime;
								
								//未到延迟入库时间
								if (timeDiff < delayWaitTimmer) {
									
									currentDelayUpdateAction = updateAction;

									//等待
									Thread.sleep(delayWaitTimmer - timeDiff);
								}
								
								//执行入库
								updateAction.doRunTask();
							}
							
							if (Thread.interrupted()) {
								break;
							}
							//获取下一个有效的操作元素
							updateAction = updateQueue.poll();

						} while (true);

					} catch (Exception e) {
						e.printStackTrace();

						if (updateAction != null && updateAction.persistAction != null) {
							logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!" + updateAction.persistAction.getPersistInfo(), e);
						} else {
							logger.error("执行批量入库时产生异常! 如果是主键冲突异常可忽略!", e);
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
	public <T extends IEntity<?>> void handleSave(final CacheObject<T> cacheObject, final DbAccessService dbAccessService, final CacheConfig<T> cacheConfig) {
		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				// 判断是否有效
				if(!this.valid()) {
					return;
				}

				Object entity = cacheObject.getEntity();

				// 持久化前操作
				cacheObject.doBeforePersist(cacheConfig);

				// 持久化
				dbAccessService.save(entity);

				// 设置更新状态
				cacheObject.setPersistStatus(PersistStatus.PERSIST);
			}

			@Override
			public String getPersistInfo() {

				// 判断状态有效性
				if(!this.valid()) {
					return null;
				}

				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}

			@Override
			public boolean valid() {
				return cacheObject.getPersistStatus() == PersistStatus.TRANSIENT;
			}

		});
	}

	@Override
	public <T extends IEntity<?>> void handleUpdate(final CacheObject<T> cacheObject, final DbAccessService dbAccessService, final CacheConfig<T> cacheConfig) {

		// 改变更新状态
		if (cacheObject.isUpdateProcessing() || !cacheObject.swapUpdateProcessing(true)) {
			return;
		}
		
		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				// 改变更新状态
				if (cacheObject.swapUpdateProcessing(false)) {

					// 持久化前的操作
					cacheObject.doBeforePersist(cacheConfig);

					//持久化
					if (cacheConfig.isEnableDynamicUpdate()) {
						dbAccessService.update(cacheObject.getEntity(), cacheObject.getModifiedFields());
					} else {
						dbAccessService.update(cacheObject.getEntity());
					}
				}
			}

			@Override
			public String getPersistInfo() {
				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}

			@Override
			public boolean valid() {
				return true;
			}

		});
	}

	@Override
	public void handleDelete(final CacheObject<?> cacheObject, final DbAccessService dbAccessService, final Object key, final CacheUnit cacheUnit) {

		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				// 判断是否有效
				if (!this.valid()) {
					return;
				}

				// 持久化
				dbAccessService.delete(cacheObject.getEntity());

				// 从缓存中移除
				cacheUnit.put(key, null);

			}

			@Override
			public String getPersistInfo() {
				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}


			@Override
			public boolean valid() {
				return cacheObject.getPersistStatus() == PersistStatus.DELETED;
			}


		});
	}


	/**
	 * 提交持久化任务
	 * @param persistAction
	 */
	private void handlePersist(PersistAction persistAction) {
		updateQueue.add(QueuedAction.valueOf(persistAction));
	}


	@Override
	public void awaitTermination() {
		// 关闭消费入库线程池
		ThreadUtils.shundownThreadPool(DB_POOL_SERVICE, true);
				
		int failCount = 0;
		while (failCount < 3) {
			try {
				//刷新所有延时入库的实体到库中
				this.flushAllEntity();
				break;
			} catch (Exception e) {
				failCount ++;
				e.printStackTrace();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}


	/**
	 * 持久化所有实体
	 */
	public void flushAllEntity() {
		//入库延迟队列中的实体
		QueuedAction updateAction = this.updateQueue.poll();
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
		QueuedAction updateAction = null;
		for (Iterator<QueuedAction> it = this.updateQueue.iterator(); it.hasNext();) {
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
