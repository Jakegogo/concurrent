package dbcache.service.impl;

import dbcache.conf.CacheConfig;
import dbcache.model.CacheObject;
import dbcache.model.PersistAction;
import dbcache.model.PersistStatus;
import dbcache.service.*;
import dbcache.utils.JsonUtils;
import dbcache.utils.NamedThreadFactory;
import dbcache.utils.ThreadUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
	private volatile ConcurrentLinkedQueue<PersistAction> updateQueue = new ConcurrentLinkedQueue<PersistAction>();
	
	/**
	 * 更改实体队列1
	 */
	private volatile ConcurrentLinkedQueue<PersistAction> swapQueue = new ConcurrentLinkedQueue<PersistAction>();
	
	/**
	 * 分类批量任务队列
	 */
	private final BatchTasks batchTasks = new BatchTasks();
	

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
	 * 分类批量任务
	 * @author Jake
	 *
	 */
	static class BatchTasks {
		
		final Map<Class<?>, LinkedList<CacheObject<?>>> saveBatchQueue = new HashMap<Class<?>, LinkedList<CacheObject<?>>>();
		
		final Map<Class<?>, LinkedList<CacheObject<?>>> updateBatchQueue = new HashMap<Class<?>, LinkedList<CacheObject<?>>>();
		
		final Map<Class<?>, LinkedList<CacheObject<?>>> deleteBatchQueue = new HashMap<Class<?>, LinkedList<CacheObject<?>>>();
		
		// 添加插入数据任务
		public void addSaveTask(CacheObject<?> object) {
			LinkedList<CacheObject<?>> list = saveBatchQueue.get(object.getEntity().getClass());
			if (list == null) {
				list = new LinkedList<CacheObject<?>>();
				saveBatchQueue.put(object.getEntity().getClass(), list);
			}
			list.add(object);
		}
		
		// 添加更新数据任务
		public void addUpdateTask(CacheObject<?> object) {
			LinkedList<CacheObject<?>> list = updateBatchQueue.get(object.getEntity().getClass());
			if (list == null) {
				list = new LinkedList<CacheObject<?>>();
				updateBatchQueue.put(object.getEntity().getClass(), list);
			}
			list.add(object);
		}
		
		// 添加更新数据任务
		public void addDeleteTask(CacheObject<?> object) {
			LinkedList<CacheObject<?>> list = deleteBatchQueue.get(object.getEntity().getClass());
			if (list == null) {
				list = new LinkedList<CacheObject<?>>();
				deleteBatchQueue.put(object.getEntity().getClass(), list);
			}
			list.add(object);
		}
		
	}
	
	
	// 批量入库操作
	protected void flushBatchTask() {
		// 保存
		for (Entry<Class<?>, LinkedList<CacheObject<?>>> entry : this.batchTasks.saveBatchQueue.entrySet()) {
			try {
				LinkedList<CacheObject<?>> list = entry.getValue();
				if (list.isEmpty()) {
					continue;
				}
				List<Object> entityList = new ArrayList<Object>();
				for (CacheObject<?> cacheObj : list) {
					if (cacheObj.getPersistStatus() != PersistStatus.DELETED) {
						entityList.add(cacheObj.getEntity());
					}
				}
				this.dbAccessService.save(entry.getKey(), entityList);
				list.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 更新
		for (Entry<Class<?>, LinkedList<CacheObject<?>>> entry : this.batchTasks.updateBatchQueue.entrySet()) {
			try {
				LinkedList<CacheObject<?>> list = entry.getValue();
				if (list.isEmpty()) {
					continue;
				}
				List<Object> entityList = new ArrayList<Object>();
				for (CacheObject<?> cacheObj : list) {
					if (cacheObj.getPersistStatus() != PersistStatus.DELETED) {
						entityList.add(cacheObj.getEntity());
					}
				}
				this.dbAccessService.update(entry.getKey(), entityList);
				list.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 删除
		for (Entry<Class<?>, LinkedList<CacheObject<?>>> entry : this.batchTasks.deleteBatchQueue.entrySet()) {
			try {
				LinkedList<CacheObject<?>> list = entry.getValue();
				if (list.isEmpty()) {
					continue;
				}
				List<Object> entityList = new ArrayList<Object>();
				for (CacheObject<?> cacheObj : list) {
					if (cacheObj.getPersistStatus() == PersistStatus.DELETED) {
						entityList.add(cacheObj.getEntity());
					}
				}
				this.dbAccessService.delete(entry.getKey(), entityList);
				list.clear();
			} catch (Exception e) {
				e.printStackTrace();
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

				ConcurrentLinkedQueue<PersistAction> processQueue = updateQueue;
				PersistAction persistAction = processQueue.poll();
				
				//循环定时检测入库,失败自动进入重试
				while (!Thread.interrupted()) {
					
					try {
						
						long timeDiff = 0l;
						long lastFlush = System.currentTimeMillis();
						do {
							
							if (persistAction == null) {
								// 执行批量入库任务
								flushBatchTask();
								// 等待下一个检测时间
								Thread.sleep(delayCheckTimmer);
							}

							timeDiff = System.currentTimeMillis() - lastFlush;
							
							if (timeDiff >= delayWaitTimmer) {
								// 替换updateQueue
								if (processQueue == updateQueue) {
									updateQueue = swapQueue;
								}
								
								do {
									//执行入库
									if (persistAction != null && persistAction.valid()) {
										persistAction.run();
									}
									if (Thread.interrupted()) {
										break;
									}
								} while ((persistAction = processQueue.poll()) != null); // 获取下一个有效的操作元素
								
								// 执行批量入库任务
								flushBatchTask();
								
								lastFlush = System.currentTimeMillis();
								
								swapQueue = processQueue;
								processQueue = updateQueue;
								
							} else {
								// 等待
								Thread.sleep(timeDiff);
							}
							
						} while (!Thread.interrupted());

					} catch (Exception e) {
						e.printStackTrace();

						if (persistAction != null) {
							logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!" + persistAction.getPersistInfo(), e);
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
	public void handleSave(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {
		this.handlePersist(new PersistAction() {

			@Override
			public void run() {

				// 判断是否有效
				if(!this.valid()) {
					return;
				}

				// 持久化前操作
				cacheObject.doBeforePersist();

				// 添加持久化任务到批量任务队列
				batchTasks.addSaveTask(cacheObject);

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
	public void handleUpdate(final CacheObject<?> cacheObject, final DbAccessService dbAccessService, CacheConfig<?> cacheConfig) {

		// 改变更新状态
		if (cacheObject.isUpdateProcessing() || !cacheObject.swapUpdateProcessing(true)) {
			return;
		}

		//最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();

		this.handlePersist(new PersistAction() {

			@Override
			public void run() {


				// 改变更新状态
				if (cacheObject.swapUpdateProcessing(false)) {

					// 持久化前的操作
					cacheObject.doBeforePersist();

					//缓存对象在提交之后被修改过
					if(editVersion < cacheObject.getEditVersion()) {
						return;
					}

					// 添加持久化任务到批量任务队列
					batchTasks.addUpdateTask(cacheObject);
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

				// 判断是否有效
				if (!this.valid()) {
					return;
				}

				// 添加持久化任务到批量任务队列
				batchTasks.addDeleteTask(cacheObject);

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
				return cacheObject.getPersistStatus() == PersistStatus.DELETED;
			}


		});
	}


	/**
	 * 提交持久化任务
	 * @param persistAction
	 */
	private void handlePersist(PersistAction persistAction) {
		updateQueue.add(persistAction);
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
		PersistAction updateAction = this.updateQueue.poll();
		while (updateAction != null) {
			//执行入库
			updateAction.run();
			updateAction = this.updateQueue.poll();
		}
		
		updateAction = this.swapQueue.poll();
		while (updateAction != null) {
			//执行入库
			updateAction.run();
			updateAction = this.swapQueue.poll();
		}
		
		// 执行批量入库任务
		this.flushBatchTask();
	}



	@Override
	public void logHadNotPersistEntity() {
		PersistAction updateAction = null;
		for (Iterator<PersistAction> it = this.updateQueue.iterator(); it.hasNext();) {
			updateAction = it.next();
			String persistInfo = updateAction.getPersistInfo();
			if(!StringUtils.isBlank(persistInfo)) {
				logger.error("检测到可能未入库对象! " + persistInfo);
			}
		}
		for (Iterator<PersistAction> it = this.swapQueue.iterator(); it.hasNext();) {
			updateAction = it.next();
			String persistInfo = updateAction.getPersistInfo();
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
