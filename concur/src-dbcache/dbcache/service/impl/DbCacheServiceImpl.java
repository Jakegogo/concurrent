package dbcache.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;



import dbcache.model.BaseModel;
import dbcache.model.CacheObject;
import dbcache.model.EntityInitializer;
import dbcache.model.FlushMode;
import dbcache.model.UpdateAction;
import dbcache.model.UpdateStatus;
import dbcache.model.UpdateType;
import dbcache.service.Cache;
import dbcache.service.DbAccessService;
import dbcache.service.DbCacheService;
import dbcache.service.DbRuleService;
import dbcache.utils.JsonUtils;
import dbcache.utils.ThreadUtils;


/**
 * 数据库缓存服务实现类
 * @author jake
 * @date 2014-7-31-下午6:07:37
 */
@Component
public class DbCacheServiceImpl implements DbCacheService, ApplicationListener<ContextClosedEvent> {
	
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DbCacheServiceImpl.class);
	
	/**
	 * 缺省入库线程池容量
	 */
	private static final int DEFAULT_DB_POOL_SIZE = Runtime.getRuntime().availableProcessors();

	/**
	 * 等待锁map {key:lock}
	 */
	private final ConcurrentMap<String, Lock> WAITING_LOCK_MAP = new ConcurrentHashMap<String, Lock>();
	
	/**
	 * 更改实体队列
	 */
	private final ConcurrentLinkedQueue<UpdateAction> updateQueue = new ConcurrentLinkedQueue<UpdateAction>();
	
	/**
	 * 入库线程池
	 */
	private ExecutorService DB_POOL_SERVICE;
	
	
	@Autowired
	private DbAccessService dbAccessService;
	
	
	@Autowired
	@Qualifier("concurrentLinkedHashMapCache")
	private Cache cache;
	
	
	@Autowired
	private DbRuleService dbRuleService;
	
	
	/**
	 * 当前延迟更新的动作
	 */
	private volatile UpdateAction currentDelayUpdateAction;
	
	
	/**
	 * dbCache 初始化
	 */
	@PostConstruct
	private void init() {
		
		//初始化dbCacheRule
		dbRuleService.init();
		
		// 初始化入库线程
		ThreadGroup threadGroup = new ThreadGroup("缓存模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "入库线程池");
		int dbPoolSize = dbRuleService.getDbPoolSize();
		if(dbPoolSize <= 0) {
			dbPoolSize = DEFAULT_DB_POOL_SIZE;
		}
		DB_POOL_SERVICE = Executors.newFixedThreadPool(dbPoolSize, threadFactory);
		
		//初始化延时入库检测线程
		final long delayWaitTimmer = dbRuleService.getDelayWaitTimmer();//延迟入库时间
		final long delayCheckTimmer = 1000;//延迟入库队列检测时间间隔
		DB_POOL_SERVICE.submit(new Runnable() {
			
			/**
			 * 延迟入库锁对象
			 */
			private Object async = new Object();
			
			@Override
			public void run() {
				
				long timeDiff = 0l;
				UpdateAction updateAction = null;
				CacheObject cacheObj = null;
				
				while(true) {
					
					updateAction = updateQueue.poll();
					
					if(updateAction == null) {
						LockSupport.parkNanos(async, delayCheckTimmer);
						continue;
					}
					cacheObj = updateAction.getCacheObject();
					//缓存对象在提交之后被修改过
					if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
						continue;
					}
					timeDiff = System.currentTimeMillis() - updateAction.getCreateTime();
					//未到延迟入库时间
					if(timeDiff < delayWaitTimmer) {
						currentDelayUpdateAction = updateAction;
						//等待
						LockSupport.parkNanos(async, delayWaitTimmer - timeDiff);
					}
					//缓存对象在提交之后被修改过
					if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
						continue;
					}
					//提交入库任务
					submitTask(updateAction);
				}
				
			}
		});
		
		//注册jvm关闭钩子
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				logNotPersistEntity();
			}
			
		});
		
	}
	
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		this.onCloseApplication();
	}
	
	
	/**
	 * 关闭应用时回调
	 */
	public void onCloseApplication() {
		//刷新所有延时入库的实体到库中
		this.flushAllEntity();
		//关闭消费入库线程池
		ThreadUtils.shundownThreadPool(DB_POOL_SERVICE, false);
		//输出为持久化的实体日志
		this.logNotPersistEntity();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Serializable id, Class<T> entityClazz) {
		
		CacheObject cacheObject = this.get(entityClazz, id);
		if (cacheObject != null) {
			return (T) cacheObject.getEntity();
		}
		
		return null;
	}
	
	
	/**
	 * 获取缓存对象
	 * @param entityClazz 实体类型
	 * @param id 实体id
	 * @return
	 */
	private <T> CacheObject get(Class<T> entityClazz, Serializable id) {
		String key = this.getEntityIdKey(id, entityClazz);
		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		if(wrapper != null) {	// 已经缓存
			CacheObject cacheObject = (CacheObject) wrapper.get();
			if(cacheObject != null && cacheObject.getUpdateStatus() != UpdateStatus.DELETED) {
				return cacheObject;
			}
			return null;
		}
		
		Lock lock = new ReentrantLock();
		Lock prevLock = WAITING_LOCK_MAP.putIfAbsent(key, lock);
		lock = prevLock != null ? prevLock : lock;
		
		CacheObject cacheObject = null;
		lock.lock();
		try {
			
			wrapper = (Cache.ValueWrapper) cache.get(key);
			if (wrapper == null) {
				T entity = dbAccessService.get(entityClazz, id);
				if (entity != null) {						
					if(entity instanceof EntityInitializer){
						EntityInitializer entityInitializer = (EntityInitializer) entity;
						entityInitializer.doAfterLoad();
					}
					
					cacheObject = new CacheObject(entity, id, entityClazz);						
					wrapper = cache.putIfAbsent(key, cacheObject);
					if (wrapper != null && wrapper.get() != null) {
						cacheObject = (CacheObject) wrapper.get();
					}
				} else {
					wrapper = cache.putIfAbsent(key, null);
					if (wrapper != null && wrapper.get() != null) {
						cacheObject = (CacheObject) wrapper.get();
					}
				}
			}
			
		} finally {
			WAITING_LOCK_MAP.remove(key);
			lock.unlock();
		}
		
		if (cacheObject != null && cacheObject.getUpdateStatus() != UpdateStatus.DELETED) {
			return cacheObject;
		}
		
		return null;
	}
	

	/**
	 * 取得实体key
	 * @param id 主键id
	 * @param entityClazz 实体类型
	 * @return String key
	 */
	public String getEntityIdKey(Serializable id, Class<?> entityClazz) {
		return new StringBuilder().append(entityClazz.getName())
									.append("_")
									.append(id).toString();
	}


	@Override
	public <T, PK extends Serializable> List<T> getEntityFromIdList(
			Collection<PK> idList, Class<T> entityClazz) {
		if (idList == null || idList.size() == 0) {
			return null;
		}
		
		List<T> list = new ArrayList<T> (idList.size());
		
		for (Serializable id : idList) {
			T entity = this.get(id, entityClazz);
			if (entity != null) {
				list.add(entity);
			}
		}
		
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseModel<PK>, PK extends Comparable<PK> & Serializable> T submitNew2Queue(T entity) {
		
		//生成主键
		if (entity.getId() == null) {
			
			Object id = this.dbRuleService.getIdAutoGenerateValue(entity.getClass());
			if (id == null) {
				String msg = "提交新建实体到更新队列参数错：未能识别主键类型";
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
			entity.setId( (PK) id);
		}
		
		//存储到缓存
		CacheObject cacheObject = null;
		String key = this.getEntityIdKey(entity.getId(), entity.getClass());
		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		
		if (wrapper == null) {//缓存还不存在
			cacheObject = new CacheObject(entity, entity.getId(), entity.getClass(), UpdateStatus.PERSIST);
			wrapper = cache.putIfAbsent(key, cacheObject);
			
			cacheObject = (CacheObject) wrapper.get();
		} else {
			cacheObject = (CacheObject) wrapper.get();
			if(cacheObject.getUpdateStatus() == UpdateStatus.DELETED) {//已被删除
				//删除再保存，实际上很少出现这种情况
				cacheObject.setUpdateStatus(UpdateStatus.PERSIST);
				wrapper = cache.putIfAbsent(key, cacheObject);
				
				cacheObject = (CacheObject) wrapper.get();
			}
		}
		
		//入库
		if (cacheObject != null) {
			//更新修改版本号
			long editVersion = cacheObject.increseEditVersion();
			long dbVersion = cacheObject.getDbVersion();
			UpdateAction updateAction = UpdateAction.valueOf(cacheObject, UpdateType.INSERT, editVersion, dbVersion);
			this.submitTask(updateAction);
		}
		
		Object obj = this.get(entity.getId(),  entity.getClass());
		return (T) obj;
	}


	@Override
	public void submitUpdated2Queue(Serializable id, Class<?> entityClazz) {
		this.submitUpdated2Queue(id, entityClazz, FlushMode.INTIME);
	}


	@Override
	public void submitUpdated2Queue(Serializable id, Class<?> entityClazz,
			FlushMode flushMode) {
		CacheObject cacheObject = this.get(entityClazz, id);
		if (cacheObject != null) {
			//更新修改版本号
			long editVersion = cacheObject.increseEditVersion();
			long dbVersion = cacheObject.getDbVersion();
			UpdateAction updateAction = UpdateAction.valueOf(cacheObject, UpdateType.UPDATE, editVersion, dbVersion);
			if(flushMode == FlushMode.DELAY) {
				this.addToUpdateQueue(updateAction);
			} else {
				this.submitTask(updateAction);
			}
		}
	}

	/**
	 * 提交到更新队列
	 * @param updateAction
	 */
	private void addToUpdateQueue(UpdateAction updateAction) {
		this.updateQueue.add(updateAction);
	}

	
	@Override
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
	public void submitDeleted2Queue(Serializable id, Class<?> entityClazz) {
		CacheObject cacheObject = this.get(entityClazz, id);
		if (cacheObject != null) {
			cacheObject.setUpdateStatus(UpdateStatus.DELETED);
			//更新修改版本号
			long editVersion = cacheObject.increseEditVersion();
			long dbVersion = cacheObject.getDbVersion();
			UpdateAction updateAction = UpdateAction.valueOf(cacheObject, UpdateType.DELETE, editVersion, dbVersion);
			this.submitTask(updateAction);
		}
	}
	
	
	/**
	 * 提交任务
	 * @param cacheObj CacheObject
	 */
	private void submitTask(UpdateAction updateAction) {
		Runnable task = this.createTask(updateAction);
		
		try {
			DB_POOL_SERVICE.submit(task);
		} catch (RejectedExecutionException ex) {
			logger.error("提交任务到更新队列产生异常", ex);
			
			this.handleTask(updateAction);
			
		} catch (Exception ex) {
			logger.error("提交任务到更新队列产生异常", ex);
		}
	}
	
	
	/**
	 * 提交延时入库任务
	 */
	@Override
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
	 * 创建入库任务
	 * @param updateAction 更新动作
	 * @return Runnable
	 */
	private Runnable createTask(final UpdateAction updateAction) {
		return new Runnable() {			
			@Override
			public void run() {
				handleTask(updateAction);
			}
		};
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
	
	
	/**
	 * 处理入库及回调事宜
	 * @param cacheObj 实体缓存
	 */
	private void handleTask(final UpdateAction updateAction) {
		//执行入库
		doSyncDb(updateAction);
	}
	
	
	/**
	 * 同步到db
	 * @param cacheObj 实体缓存
	 */
	private void doSyncDb(final UpdateAction updateAction) {
		if (updateAction == null || updateAction.getCacheObject() == null) {
			return;
		}
		
		CacheObject cacheObj = updateAction.getCacheObject();
		
		//缓存对象在提交之后被修改过
		if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
			return;
		}
		
		//比较并更新入库版本号
		if (!cacheObj.compareAndUpdateDbSync(updateAction)) {
			return;
		}
		
		try {
			
			//持久化前操作
			Object entity = cacheObj.getEntity();
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
				String key = this.getEntityIdKey(cacheObj.getId(), cacheObj.getClass());
				cache.evict(key);
			} else if (updateAction.getUpdateType() == UpdateType.INSERT) {
				dbAccessService.save(cacheObj.getEntity());
			} else {
				dbAccessService.update(cacheObj.getEntity());
			}
			
		} catch (Exception ex) {
			logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!", ex);
		}
			
	}
	
	
	/**
	 * 打印出未入库对象
	 */
	private void logNotPersistEntity() {
		UpdateAction updateAction = null;
		CacheObject cacheObject = null;
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
	 * 获取入库线程池
	 * @return
	 */
	@Override
	public ExecutorService getThreadPool() {
		return this.DB_POOL_SERVICE;
	}
	
	
	/**
	 * 可命名线程工厂
	 * @author jake
	 * @date 2014-8-2-下午5:14:10
	 */
	public static class NamedThreadFactory implements ThreadFactory {

		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;

		public NamedThreadFactory(ThreadGroup group, String name) {
			this.group = group;
			namePrefix = group.getName() + ":" + name;
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			return t;
		}

	}


}
