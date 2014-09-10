package dbcache.service.impl;

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
	private static final int DEFAULT_DB_POOL_SIZE = Runtime.getRuntime().availableProcessors();


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
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "即时入库线程池");
		int dbPoolSize = dbRuleService.getDbPoolSize();
		if(dbPoolSize <= 0) {
			dbPoolSize = DEFAULT_DB_POOL_SIZE;
		}
		DB_POOL_SERVICE = Executors.newFixedThreadPool(dbPoolSize, threadFactory);

		this.cache = cache;
	}


	@Override
	public void handlerPersist(UpdateAction updateAction) {
		submitTask(updateAction);
	}

	/**
	 * 提交任务
	 * @param cacheObj CacheObject
	 */
	private void submitTask(UpdateAction updateAction) {
		Runnable task = this.createTask(updateAction);
		if(task == null) {
			return;
		}

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
	 * 创建入库任务
	 * @param updateAction 更新动作
	 * @return Runnable
	 */
	private Runnable createTask(final UpdateAction updateAction) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				handleTask(updateAction);
			}
		};

		//提交任务之前先判断缓存对象在提交之后被修改过
		CacheObject<?> cacheObj = updateAction.getCacheObject();

		if(updateAction.getEditVersion() < cacheObj.getEditVersion()) {
			return null;
		}

		return runnable;
	}


	/**
	 * 处理入库及回调事宜
	 * @param cacheObj 实体缓存
	 */
	private void handleTask(final UpdateAction updateAction) {
		//执行入库
		doSyncDb(updateAction);
	}


	@Override
	public void awaitTermination() {
		//关闭消费入库线程池
		ThreadUtils.shundownThreadPool(DB_POOL_SERVICE, false);
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


}
