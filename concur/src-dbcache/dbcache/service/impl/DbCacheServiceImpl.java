package dbcache.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import dbcache.annotation.ThreadSafe;
import dbcache.conf.CacheConfig;
import dbcache.conf.CacheRule;
import dbcache.conf.Inject;
import dbcache.model.CacheObject;
import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;
import dbcache.model.IndexValue;
import dbcache.model.PersistAction;
import dbcache.model.UpdateStatus;
import dbcache.service.Cache;
import dbcache.service.ConfigFactory;
import dbcache.service.DbAccessService;
import dbcache.service.DbCacheService;
import dbcache.service.DbIndexService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.support.asm.ValueGetter;
import dbcache.utils.JsonUtils;


/**
 * 数据库缓存服务实现类
 * <br/>更改实体属性值需要外部加锁
 * @author jake
 * @date 2014-7-31-下午6:07:37
 */
@ThreadSafe
@Component
public class DbCacheServiceImpl<T extends IEntity<PK>, PK extends Comparable<PK> & Serializable>
		implements DbCacheService<T, PK>, ApplicationListener<ContextClosedEvent> {

	/**
	 * 实现原则:
	 * 1,不需要用锁的地方尽量不用到锁;横向扩展设计,减少并发争用资源
	 * 2,维护缓存原子性,数据入库采用类似异步事件驱动方式
	 * 3,支持大批量操作数据
	 * 4,积极解耦,模块/组件的方式,基于接口的设计,易于维护和迁移
	 * 5,用户不需要了解太多的内部原理,不需要太多配置
	 * 6,可监控,易于问题排查
	 */

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DbCacheServiceImpl.class);

	/**
	 * 实体类形
	 * 需要外部设定值
	 */
	@Inject
	private Class<T> clazz;

	/**
	 * 实体缓存配置
	 */
	@Inject
	private CacheConfig<T> cacheConfig;

	/**
	 * 等待锁map {key:lock}
	 */
	private final ConcurrentMap<Object, Lock> WAITING_LOCK_MAP = new ConcurrentHashMap<Object, Lock>();


	@Autowired
	private ConfigFactory configFactory;


	@Autowired
	@Qualifier("hibernateDbAccessServiceImpl")
	private DbAccessService dbAccessService;


	@Autowired
	@Qualifier("concurrentLinkedHashMapCache")
	private Cache cache;


	@Autowired
	private DbRuleService dbRuleService;

	/**
	 * 默认的持久化服务
	 */
	@Autowired
	@Qualifier("inTimeDbPersistService")
	private DbPersistService dbPersistService;

	/**
	 * 索引服务
	 */
	@Autowired
	private DbIndexService<PK> indexService;

	/**
	 * dbCache 初始化
	 */
	@PostConstruct
	private void init() {

		//初始化dbCacheRule
		dbRuleService.init();

		//初始化持久化服务
		dbPersistService.init();

		//注册jvm关闭钩子
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				dbPersistService.logHadNotPersistEntity();
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
		//等待入库执行完毕
		dbPersistService.awaitTermination();
		//输出为持久化的实体日志
		dbPersistService.logHadNotPersistEntity();
	}


	@Override
	public T get(PK id) {

		final CacheObject<T> cacheObject = this.get(clazz, id);
		if (cacheObject != null) {
			return (T) cacheObject.getProxyEntity();
		}

		return null;
	}


	/**
	 * 获取缓存对象
	 * @param entityClazz 实体类型
	 * @param id 实体id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private CacheObject<T> get(Class<T> entityClazz, Serializable id) {

		final Object key = CacheRule.getEntityIdKey(id, entityClazz);

		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		if(wrapper != null) {	// 已经缓存

			CacheObject<T> cacheObject = (CacheObject<T>) wrapper.get();
			if(cacheObject != null && cacheObject.getUpdateStatus() != UpdateStatus.DELETED) {
				return cacheObject;
			}

			return null;
		}


		// 获取缓存唯一锁
		Lock lock = new ReentrantLock();;
		Lock prevLock = WAITING_LOCK_MAP.putIfAbsent(key, lock);
		lock = prevLock != null ? prevLock : lock;

		CacheObject<T> cacheObject = null;
		lock.lock();
		try {

			wrapper = (Cache.ValueWrapper) cache.get(key);
			if (wrapper == null) {

				T entity = dbAccessService.get(entityClazz, id);
				if (entity != null) {
					// 调用初始化
					if(entity instanceof EntityInitializer){
						EntityInitializer entityInitializer = (EntityInitializer) entity;
						entityInitializer.doAfterLoad();
					}

					// 创建缓存对象
					cacheObject = (CacheObject<T>) configFactory.createCacheObject(entity, entityClazz, indexService, key, cache, UpdateStatus.PERSIST);

					wrapper = cache.putIfAbsent(key, cacheObject);

					if (wrapper != null && wrapper.get() != null) {
						cacheObject = (CacheObject<T>) wrapper.get();

						// 更新索引 需要外层加锁
						entity = cacheObject.getEntity();
						if(cacheConfig.isEnableIndex()) {
							for(Map.Entry<String, ValueGetter<T>> entry : cacheConfig.getIndexes().entrySet()) {
								Object indexValue = entry.getValue().get(entity);
								this.indexService.create(IndexValue.valueOf(entry.getKey(), indexValue, entity.getId()));
							}
						}
					}
				} else {
					// 缓存NULL value
					wrapper = cache.putIfAbsent(key, null);
					if (wrapper != null && wrapper.get() != null) {
						cacheObject = (CacheObject<T>) wrapper.get();
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


	@Override
	public List<T> getEntityFromIdList(Collection<PK> idList) {

		if (idList == null || idList.size() == 0) {
			return null;
		}

		final List<T> list = new ArrayList<T> (idList.size());

		for (PK id : idList) {
			T entity = this.get(id);
			if (entity != null) {
				list.add(entity);
			}
		}

		return list;
	}


	@Override
	public List<T> listByIndex(String indexName, Object indexValue) {

		final Collection<PK> idList = this.indexService.get(indexName, indexValue);
		if(idList == null || idList.isEmpty()) {
			return Collections.emptyList();
		}

		final List<T> result = new ArrayList<T>(idList.size());
		T temp = null;
		for(PK id : idList) {
			temp = this.get(id);
			if(temp != null) {
				result.add(temp);
			}
		}
		return result;
	}



	@Override
	public Collection<PK> listIdByIndex(String indexName, Object indexValue) {
		return this.indexService.get(indexName, indexValue);
	}


	@Override
	public List<T> pageByIndex(String indexName, Object indexValue, int page,
			int size) {

		final Collection<PK> idList = this.indexService.get(indexName, indexValue);
		if(idList == null || idList.isEmpty()) {
			return Collections.emptyList();
		}

		int startIndex = (page - 1) * size;
		int endIndex = page * size;
		if(startIndex < 0) {
			startIndex = 0;
		}
		if(endIndex > idList.size()) {
			endIndex = idList.size();
		}

		List<T> result = new ArrayList<T>();
		T temp = null;
		int index = 0;

		for(PK id : idList) {
			//分页操作
			if(index < startIndex) {
				continue;
			} else if(index > endIndex) {
				break;
			}

			temp = this.get(id);
			if(temp != null) {
				result.add(temp);
				index ++;
			}
		}
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public T submitNew2Queue(T entity) {

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
		CacheObject<T> cacheObject = null;
		final Object key = CacheRule.getEntityIdKey(entity.getId(), entity.getClass());
		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);

		if (wrapper == null) {//缓存还不存在

			cacheObject = (CacheObject<T>) configFactory.createCacheObject(entity, entity.getClass(), indexService, key, cache, UpdateStatus.PERSIST);

			wrapper = cache.putIfAbsent(key, cacheObject);
			if (wrapper != null && wrapper.get() != null) {
				cacheObject = (CacheObject<T>) wrapper.get();
			}

		} else {

			cacheObject = (CacheObject<T>) wrapper.get();

			if(cacheObject.getUpdateStatus() == UpdateStatus.DELETED) {//已被删除
				//删除再保存，实际上很少出现这种情况
				cacheObject.setUpdateStatus(UpdateStatus.PERSIST);
				wrapper = cache.putIfAbsent(key, cacheObject);
				if (wrapper != null && wrapper.get() != null) {
					cacheObject = (CacheObject<T>) wrapper.get();
				}

				cacheObject = (CacheObject<T>) wrapper.get();
			}
		}

		//入库
		if (cacheObject != null) {

			//更新索引
			if(cacheConfig.isEnableIndex()) {
				entity = cacheObject.getEntity();
				for(Map.Entry<String, ValueGetter<T>> entry : cacheConfig.getIndexes().entrySet()) {
					Object indexValue = entry.getValue().get(entity);
					this.indexService.create(IndexValue.valueOf(entry.getKey(), indexValue, entity.getId()));
				}
			}

			@SuppressWarnings("rawtypes")
			final CacheObject cacheObj = cacheObject;
			//最新修改版本号
			final long editVersion = cacheObject.increseEditVersion();
			final long dbVersion = cacheObject.getDbVersion();

			dbPersistService.handlerPersist(new PersistAction() {

				Object entity = cacheObj.getEntity();

				@Override
				public void run() {

					//缓存对象在提交之后被修改过
					if(editVersion < cacheObj.getEditVersion()) {
						return;
					}

					//比较并更新入库版本号
					if (!cacheObj.compareAndUpdateDbSync(dbVersion, editVersion)) {
						return;
					}

					//持久化前操作
					if(entity instanceof EntityInitializer){
						EntityInitializer entityInitializer = (EntityInitializer) entity;
						entityInitializer.doBeforePersist();
					}

					//缓存对象在提交之后被入库过
					if(cacheObj.getDbVersion() > editVersion) {
						return;
					}

					//持久化
					dbAccessService.save(entity);
				}

				@Override
				public String getPersistInfo() {

					//缓存对象在提交之后被修改过
					if(editVersion < cacheObj.getEditVersion()) {
						return null;
					}

					return JsonUtils.object2JsonString(cacheObj.getEntity());
				}

				@Override
				public boolean valid() {
					return editVersion == cacheObj.getEditVersion();
				}

			});

		}

		return (T) this.get(entity.getId());
	}


	@Override
	public void submitUpdated2Queue(T entity) {

		final CacheObject<T> cacheObject = this.get(clazz, entity.getId());

		if (cacheObject != null) {
			// 验证缓存操作原子性(缓存实体必须唯一)
			if(cacheObject.getProxyEntity() != entity) {
				String msg = "实体使用期间缓存对象CacheObject被修改过:无法保证原子性和实体唯一,请重试[current:"
						+ JsonUtils.object2JsonString(entity) + ", remote:" + JsonUtils.object2JsonString(cacheObject.getProxyEntity()) + "]";
				logger.error(msg);
				throw new IllegalStateException(msg);
			}

			//最新修改版本号
			final long editVersion = cacheObject.increseEditVersion();
			final long dbVersion = cacheObject.getDbVersion();

			dbPersistService.handlerPersist(new PersistAction() {

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
	}


	@Override
	public void submitDeleted2Queue(T entity) {
		submitDeleted2Queue(entity.getId());
	}


	@Override
	public void submitDeleted2Queue(final PK id) {

		final CacheObject<T> cacheObject = this.get(clazz, id);

		if (cacheObject != null) {
			//标记为已经删除
			cacheObject.setUpdateStatus(UpdateStatus.DELETED);

			//更新索引
			if(cacheConfig.isEnableIndex()) {
				T entity = cacheObject.getEntity();
				for(Map.Entry<String, ValueGetter<T>> entry : cacheConfig.getIndexes().entrySet()) {
					Object indexValue = entry.getValue().get(entity);
					this.indexService.remove(IndexValue.valueOf(entry.getKey(), indexValue, entity.getId()));
				}
			}

			//最新修改版本号
			final long editVersion = cacheObject.increseEditVersion();
			final long dbVersion = cacheObject.getDbVersion();

			dbPersistService.handlerPersist(new PersistAction() {

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

					//缓存对象在提交之后被入库过
					if(cacheObject.getDbVersion() > editVersion) {
						return;
					}

					//持久化
					dbAccessService.delete(entity);
					Object key = CacheRule.getEntityIdKey(id, clazz);
					//从缓存中移除
					cache.evict(key);
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
	}


	@Override
	public ExecutorService getThreadPool() {
		return this.dbPersistService.getThreadPool();
	}


	@Override
	public Cache getCache() {
		return cache;
	}


	public CacheConfig<T> getCacheConfig() {
		return cacheConfig;
	}


	@Override
	public String toString() {
		Map<String, Object> toStrMap = new HashMap<String, Object>();
		toStrMap.put("clazz", this.clazz);
		toStrMap.put("proxyClazz", this.cacheConfig.getProxyClazz());
		toStrMap.put("WAITING_LOCK_MAP_SIZE", this.WAITING_LOCK_MAP.size());
		toStrMap.put("cacheUseSize", this.cache.getCachedSize());
		toStrMap.put("indexServiceCacheUseSize", this.indexService.getCache().getCachedSize());
		return JsonUtils.object2JsonString(toStrMap);
	}



}
