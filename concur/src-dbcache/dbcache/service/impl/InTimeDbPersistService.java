package dbcache.service.impl;

import dbcache.model.CacheObject;
import dbcache.model.EntityInitializer;
import dbcache.model.PersistAction;
import dbcache.service.DbAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.utils.JsonUtils;
import dbcache.utils.NamedThreadFactory;
import dbcache.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

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



	@PostConstruct
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

		// 初始化线程池
		DB_POOL_SERVICE = Executors.newFixedThreadPool(dbPoolSize, threadFactory);

	}


	@Override
	public void handleSave(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {
		this.handlePersist(new PersistAction() {

			Object entity = cacheObject.getEntity();

			@Override
			public void run() {

				// 判断是否有效
				if(!this.valid()) {
					return;
				}

				// 持久化前操作
				if(entity instanceof EntityInitializer){
					EntityInitializer entityInitializer = (EntityInitializer) entity;
					entityInitializer.doBeforePersist();
				}

				// 持久化
				dbAccessService.save(entity);
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
				return true;
			}

		});
	}

	@Override
	public void handleUpdate(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {
		//最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();
		final long dbVersion = cacheObject.getDbVersion();

		this.handlePersist(new PersistAction() {

			Object entity = cacheObject.getEntity();

			@Override
			public void run() {

				//缓存对象在提交之后被修改过
				if(editVersion < cacheObject.getEditVersion()) {
					return;
				}

				//比较并更新入库版本号
				if (!cacheObject.compareAndUpdateDbSync(dbVersion, editVersion)) {
					return;
				}

				//持久化前操作
				if(entity instanceof EntityInitializer){
					EntityInitializer entityInitializer = (EntityInitializer) entity;
					entityInitializer.doBeforePersist();
				}

				//缓存对象在提交之后被入库过
				if(cacheObject.getDbVersion() > editVersion) {
					return;
				}

				//持久化
				dbAccessService.update(entity);
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
	public void handleDelete(final CacheObject<?> cacheObject, final DbAccessService dbAccessService) {
		// 最新修改版本号
		final long editVersion = cacheObject.increseEditVersion();
		final long dbVersion = cacheObject.getDbVersion();

		this.handlePersist(new PersistAction() {

			Object entity = cacheObject.getEntity();

			@Override
			public void run() {

				// 缓存对象在提交之后被修改过
				if(editVersion < cacheObject.getEditVersion()) {
					return;
				}

				// 比较并更新入库版本号
				if (!cacheObject.compareAndUpdateDbSync(dbVersion, editVersion)) {
					return;
				}

				// 缓存对象在提交之后被入库过
				if(cacheObject.getDbVersion() > editVersion) {
					return;
				}

				// 持久化
				dbAccessService.delete(entity);

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
				return editVersion == cacheObject.getEditVersion();
			}


		});
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


	/**
	 * 提交持久化任务
	 * @param persistAction
	 */
	private void handlePersist(PersistAction persistAction) {

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
