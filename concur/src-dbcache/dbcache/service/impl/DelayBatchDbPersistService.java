package dbcache.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dbcache.model.CacheObject;
import dbcache.model.PersistAction;
import dbcache.model.PersistStatus;
import dbcache.service.Cache;
import dbcache.service.DbAccessService;
import dbcache.service.DbBatchAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.JsonUtils;
import dbcache.utils.NamedThreadFactory;


/**
 * 延时批量入库实现类
 * <br/>单线程执行入库
 * @author Jake
 * @date 2014年8月13日上午12:31:06
 */
@Component("delayBatchDbPersistService")
public class DelayBatchDbPersistService implements DbPersistService {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DelayBatchDbPersistService.class);

	/**
	 * 更改实体队列
	 */
	private final ConcurrentLinkedQueue<QueuedAction> updateQueue = new ConcurrentLinkedQueue<QueuedAction>();
	
	/**
	 * 分类批量任务队列
	 */
	private final BatchTasks batchTasks = new BatchTasks();
	
	/**
	 * 当前延迟更新的动作
	 */
	private volatile QueuedAction currentDelayUpdateAction;


	@Autowired
	private DbRuleService dbRuleService;

	@Autowired
	@Qualifier("jdbcDbAccessServiceImpl")
	private DbBatchAccessService dbAccessService;

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
	
	/**
	 * 分类批量任务
	 * @author Jake
	 *
	 */
	static class BatchTasks {
		
		final Map<Class<?>, LinkedList<Object>> saveBatchQueue = new HashMap<Class<?>, LinkedList<Object>>();
		
		final Map<Class<?>, LinkedList<Object>> updateBatchQueue = new HashMap<Class<?>, LinkedList<Object>>();
		
		final Map<Class<?>, LinkedList<Object>> deleteBatchQueue = new HashMap<Class<?>, LinkedList<Object>>();
		
		// 添加插入数据任务
		public void addSaveTask(Object object) {
			LinkedList<Object> list = saveBatchQueue.get(object.getClass());
			if (list == null) {
				list = new LinkedList<Object>();
				saveBatchQueue.put(object.getClass(), list);
			}
			list.add(object);
		}
		
		// 添加更新数据任务
		public void addUpdateTask(Object object) {
			LinkedList<Object> list = updateBatchQueue.get(object.getClass());
			if (list == null) {
				list = new LinkedList<Object>();
				updateBatchQueue.put(object.getClass(), list);
			}
			list.add(object);
		}
		
		// 添加更新数据任务
		public void addDeleteTask(Object object) {
			LinkedList<Object> list = deleteBatchQueue.get(object.getClass());
			if (list == null) {
				list = new LinkedList<Object>();
				deleteBatchQueue.put(object.getClass(), list);
			}
			list.add(object);
		}
		
		// 清除任务队列
		public void clearTask() {
			saveBatchQueue.clear();
			updateBatchQueue.clear();
			deleteBatchQueue.clear();
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
				while (true) {

					QueuedAction updateAction = updateQueue.poll();
					try {

						long timeDiff = 0l;
						do {

							if (updateAction == null) {
								//等待下一个检测时间
								Thread.sleep(delayCheckTimmer);
							} else {

								timeDiff = System.currentTimeMillis() - updateAction.createTime;

								// 未到延迟入库时间
								if (timeDiff < delayWaitTimmer) {
									currentDelayUpdateAction = updateAction;
									// 执行批量入库任务
									flushBatchTask();
									// 等待
									Thread.sleep(delayWaitTimmer - timeDiff);
								}

								//执行入库
								updateAction.doRunTask();
							}

							//获取下一个有效的操作元素
							updateAction = updateQueue.poll();
							while (updateAction != null && !updateAction.persistAction.valid()) {
								updateAction = updateQueue.poll();
							}

						} while (true);

					} catch (Exception e) {
						e.printStackTrace();

						if (updateAction != null && updateAction.persistAction != null) {
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

	
	// 批量入库操作
	protected void flushBatchTask() {
		// 保存
		for(Entry<Class<?>, LinkedList<Object>> entry : this.batchTasks.saveBatchQueue.entrySet()) {
			this.dbAccessService.save(entry.getKey(), entry.getValue());
		}
		// 更新
		for(Entry<Class<?>, LinkedList<Object>> entry : this.batchTasks.updateBatchQueue.entrySet()) {
			this.dbAccessService.update(entry.getKey(), entry.getValue());
		}
		// 删除
		for(Entry<Class<?>, LinkedList<Object>> entry : this.batchTasks.updateBatchQueue.entrySet()) {
			this.dbAccessService.delete(entry.getKey(), entry.getValue());
		}
		// 清空任务
		this.batchTasks.clearTask();
	}


	@Override
	public void handleSave(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {
		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				// 判断是否有效
				if(!this.valid()) {
					return;
				}

				Object entity = cacheObject.getEntity();

				// 持久化前操作
				cacheObject.doBeforePersist();

				// 添加持久化任务到批量任务队列
				batchTasks.addSaveTask(entity);

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
	public void handleUpdate(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {

		// 改变更新状态
		if (!cacheObject.swapUpdateProcessing(true)) {
			return;
		}

		//最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();

		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				//缓存对象在提交之后被修改过
				if(editVersion < cacheObject.getEditVersion()) {
					return;
				}

				// 改变更新状态
				if (cacheObject.swapUpdateProcessing(false)) {


					// 更新入库版本号
					cacheObject.setDbVersion(editVersion);

					// 持久化前的操作
					cacheObject.doBeforePersist();

					//缓存对象在提交之后被入库过
					if (cacheObject.getDbVersion() > editVersion) {
						return;
					}

					// 添加持久化任务到批量任务队列
					batchTasks.addUpdateTask(cacheObject.getEntity());
				}
			}

			@Override
			public String getPersistInfo() {

				//缓存对象在提交之后被修改过
				if(editVersion < cacheObject.getEditVersion()) {
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
	public void handleDelete(final CacheObject<?> cacheObject, final DbAccessService dbAccessService, final Object key, final Cache cache) {
		// 最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();

		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				// 缓存对象在提交之后被修改过
				if(editVersion < cacheObject.getEditVersion()) {
					return;
				}

				// 缓存对象在提交之后被入库过
				if(cacheObject.getDbVersion() > editVersion) {
					return;
				}

				// 添加持久化任务到批量任务队列
				batchTasks.addDeleteTask(cacheObject.getEntity());

				// 从缓存中移除
				cache.put(key, null);

			}

			@Override
			public String getPersistInfo() {

				// 缓存对象在提交之后被修改过
				if(editVersion < cacheObject.getEditVersion()) {
					return null;
				}

				return JsonUtils.object2JsonString(cacheObject.getEntity());
			}


			@Override
			public boolean valid() {
				return editVersion == cacheObject.getEditVersion() && cacheObject.getPersistStatus() == PersistStatus.DELETED;
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
		//刷新所有延时入库的实体到库中
		this.flushAllEntity();
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
		// 执行批量入库任务
		this.flushBatchTask();
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
