package dbcache.service.impl;

import dbcache.conf.CacheConfig;
import dbcache.model.CacheObject;
import dbcache.model.PersistAction;
import dbcache.model.PersistStatus;
import dbcache.service.CacheUnit;
import dbcache.service.DbAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.JsonUtils;
import dbcache.utils.NamedThreadFactory;
import dbcache.utils.ThreadUtils;
import dbcache.utils.executors.SimpleLinkingRunnable;
import dbcache.utils.executors.SimpleOrderedThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

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

	/**
	 * 线程池大小
	 */
	private int dbPoolSize;
	
	/**
	 * 重试实体队列
	 */
	private final ConcurrentLinkedQueue<OrderedPersistAction> retryQueue = new ConcurrentLinkedQueue<OrderedPersistAction>();

	/**
	 * 定时检测重试线程
	 */
	private Thread checkRetryThread;

	@Autowired
	private DbRuleService dbRuleService;


	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {

		// 初始化入库线程
		ThreadGroup threadGroup = new ThreadGroup("缓存模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "即时入库线程池");

		// 设置线程池大小
		int dbPoolSize = dbRuleService.getDbPoolSize();
		if(dbPoolSize <= 0) {
			dbPoolSize = DEFAULT_DB_POOL_SIZE;
		}
		if(dbPoolSize <= 0) {
			dbPoolSize = 4;
		}

		this.dbPoolSize = dbPoolSize;

		// 初始化线程池
		DB_POOL_SERVICE = SimpleOrderedThreadPoolExecutor.newFixedThreadPool(dbPoolSize, threadFactory);
		
		
		// 定时检测失败操作
		final long delayWaitTimmer = dbRuleService.getDelayWaitTimmer();//延迟入库时间(毫秒)
		// 初始化检测线程
		checkRetryThread = new Thread() {
			
			public void run() {
				OrderedPersistAction action = null;
				
				while (!Thread.interrupted()) {
					try {
						action = retryQueue.poll();
						while (action != null) {
							handlePersist(action);
							if (Thread.interrupted()) {
								break;
							}
							action = retryQueue.poll();
						}
						
						Thread.sleep(delayWaitTimmer);
					} catch (Exception e) {
						e.printStackTrace();
						
						if (action != null) {
							logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!" + action.getPersistInfo(), e);
						} else {
							logger.error("执行批量入库时产生异常! 如果是主键冲突异常可忽略!", e);
						}
						
						try {
							Thread.sleep(delayWaitTimmer);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			
		};
		checkRetryThread.start();
		
	}


	abstract class OrderedPersistAction extends SimpleLinkingRunnable implements PersistAction {

	}


	@Override
	public void handleSave(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {

		this.handlePersist(new OrderedPersistAction() {

			@Override
			public AtomicReference<SimpleLinkingRunnable> getLastSimpleLinkingRunnable() {
				return cacheObject.getLastLinkingRunnable();
			}

			@Override
			public void run() {

				// 判断是否有效
				if (!this.valid()) {
					return;
				}

				Object entity = cacheObject.getEntity();

				// 持久化前操作
				cacheObject.doBeforePersist();

				// 持久化
				dbAccessService.save(entity);

				// 设置状态为持久化
				cacheObject.setPersistStatus(PersistStatus.PERSIST);

			}
			
			@Override
			public void onException(Throwable t) {
				cacheObject.swapUpdateProcessing(false);
				retryQueue.add(this);
			}

			@Override
			public String getPersistInfo() {

				// 判断状态有效性
				if (!this.valid()) {
					return null;
				}

				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}

			@Override
			public boolean valid() {
				return cacheObject.getPersistStatus() != PersistStatus.DELETED;
			}

		});

		
	}

	@Override
	public void handleUpdate(final CacheObject<?> cacheObject, final DbAccessService dbAccessService, final CacheConfig<?> cacheConfig) {
		// 改变更新状态
		if (cacheObject.isUpdateProcessing() || !cacheObject.swapUpdateProcessing(true)) {
			return;
		}
		//最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();

		this.handlePersist(new OrderedPersistAction() {

			@Override
			public AtomicReference<SimpleLinkingRunnable> getLastSimpleLinkingRunnable() {
				return cacheObject.getLastLinkingRunnable();
			}

			@Override
			public void run() {
				
				// 改变更新状态
				if (cacheObject.swapUpdateProcessing(false)) {

					// 持久化前的操作
					cacheObject.doBeforePersist();

					//缓存对象在提交之后被修改过
					if (editVersion >= cacheObject.getEditVersion()) {
						//持久化
						if (cacheConfig.isEnableDynamicUpdate()) {
							dbAccessService.update(cacheObject.getEntity(), cacheObject.getModifiedFields());
						} else {
							dbAccessService.update(cacheObject.getEntity());
						}
					}

				} else {
					throw new IllegalStateException("检测到非顺序入库.");// 正常情况不会执行到这个分支
				}

			}

			@Override
			public void onException(Throwable t) {
				cacheObject.swapUpdateProcessing(false);
				retryQueue.add(this);
			}

			@Override
			public String getPersistInfo() {

				//缓存对象在提交之后被修改过
				if (editVersion < cacheObject.getEditVersion()) {
					return null;
				}

				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}

			@Override
			public boolean valid() {
				return editVersion == cacheObject.getEditVersion();
			}

		});

	}


	@Override
	public void handleDelete(final CacheObject<?> cacheObject, final DbAccessService dbAccessService, final Object key, final CacheUnit cacheUnit) {
		// 最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();

		this.handlePersist(new OrderedPersistAction() {

			@Override
			public AtomicReference<SimpleLinkingRunnable> getLastSimpleLinkingRunnable() {
				return cacheObject.getLastLinkingRunnable();
			}

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
			public void onException(Throwable t) {
				cacheObject.swapUpdateProcessing(false);
				retryQueue.add(this);
			}

			@Override
			public String getPersistInfo() {

				// 缓存对象在提交之后被修改过
				if (editVersion < cacheObject.getEditVersion()) {
					return null;
				}

				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}


			@Override
			public boolean valid() {
				return cacheObject.getPersistStatus() == PersistStatus.DELETED;
			}


		});

	}

	@Override
	public void awaitTermination() {
		// 中断重试线程
		checkRetryThread.interrupt();
		
		// 清空重试队列
		OrderedPersistAction action = retryQueue.poll();
		while (action != null) {
			handlePersist(action);
			action = retryQueue.poll();
		}

		// 关闭消费入库线程池
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


	/**
	 * 提交持久化任务
	 * @param persistAction
	 */
	private void handlePersist(OrderedPersistAction persistAction) {

		try {
			DB_POOL_SERVICE.execute(persistAction);
		} catch (RejectedExecutionException ex) {
			logger.error("提交任务到更新队列被拒绝,使用同步处理:RejectedExecutionException");

			this.handleTask(persistAction);

		} catch (Exception ex) {
			persistAction.onException(ex);

			logger.error("提交任务到更新队列产生异常", ex);
		}
	}

	/**
	 * 处理持久化操作
	 * @param persistAction 持久化操作
	 */
	private void handleTask(Runnable persistAction) {
		persistAction.run();
	}


}
