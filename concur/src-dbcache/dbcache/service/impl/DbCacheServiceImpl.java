package dbcache.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import dbcache.conf.CacheRule;
import dbcache.model.CacheObject;
import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;
import dbcache.model.UpdateAction;
import dbcache.model.UpdateStatus;
import dbcache.model.UpdateType;
import dbcache.proxy.util.ClassUtil;
import dbcache.service.Cache;
import dbcache.service.DbAccessService;
import dbcache.service.DbCacheService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;
import dbcache.service.EntityIndexService;


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
	 * 1,不需要用锁的地方尽量不用到锁
	 * 2,维护缓存原子性,数据入库采用类似异步事件驱动方式
	 * 3,支持大批量操作数据
	 */

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DbCacheServiceImpl.class);

	/**
	 * 实体类形
	 * 需要外部设定值
	 */
	private Class<T> clazz;

	/**
	 * 实体静态代理类
	 */
	private Class<?> proxyClazz;

	/**
	 * 等待锁map {key:lock}
	 */
	private final ConcurrentMap<String, Lock> WAITING_LOCK_MAP = new ConcurrentHashMap<String, Lock>();


	@Autowired
	private DbAccessService dbAccessService;


	@Autowired
	@Qualifier("concurrentWeekHashMapCache")
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
	private EntityIndexService<PK> indexService;

	/**
	 * dbCache 初始化
	 */
	@PostConstruct
	private void init() {

		//初始化dbCacheRule
		dbRuleService.init();

		//初始化持久化服务
		dbPersistService.init(cache);

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

		CacheObject<T> cacheObject = this.get(clazz, id);
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
		String key = CacheRule.getEntityIdKey(id, entityClazz);

		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		if(wrapper != null) {	// 已经缓存

			CacheObject<T> cacheObject = (CacheObject<T>) wrapper.get();
			if(cacheObject != null && cacheObject.getUpdateStatus() != UpdateStatus.DELETED) {
				return cacheObject;
			}

			return null;
		}

		Lock lock = new ReentrantLock();
		Lock prevLock = WAITING_LOCK_MAP.putIfAbsent(key, lock);
		lock = prevLock != null ? prevLock : lock;

		CacheObject<T> cacheObject = null;
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

					cacheObject = new CacheObject<T>(entity, id, entityClazz, ClassUtil.getProxyEntity(proxyClazz, entity, indexService));
					wrapper = cache.putIfAbsent(key, cacheObject);

					if (wrapper != null && wrapper.get() != null) {
						cacheObject = (CacheObject<T>) wrapper.get();
					}
				} else {
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

		List<T> list = new ArrayList<T> (idList.size());

		for (PK id : idList) {
			T entity = this.get(id);
			if (entity != null) {
				list.add(entity);
			}
		}

		return list;
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
		String key = CacheRule.getEntityIdKey(entity.getId(), entity.getClass());
		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);

		if (wrapper == null) {//缓存还不存在

			cacheObject = new CacheObject<T>(entity, entity.getId(), (Class<T>) entity.getClass(), ClassUtil.getProxyEntity(proxyClazz, entity), UpdateStatus.PERSIST);
			wrapper = cache.putIfAbsent(key, cacheObject);

			cacheObject = (CacheObject<T>) wrapper.get();
		} else {

			cacheObject = (CacheObject<T>) wrapper.get();

			if(cacheObject.getUpdateStatus() == UpdateStatus.DELETED) {//已被删除
				//删除再保存，实际上很少出现这种情况
				cacheObject.setUpdateStatus(UpdateStatus.PERSIST);
				wrapper = cache.putIfAbsent(key, cacheObject);

				cacheObject = (CacheObject<T>) wrapper.get();
			}
		}

		//入库
		if (cacheObject != null) {
			UpdateAction updateAction = UpdateAction.valueOf(cacheObject, UpdateType.INSERT);
			dbPersistService.handlerPersist(updateAction);
		}

		Object obj = this.get(entity.getId());
		return (T) obj;
	}


	@Override
	public void submitUpdated2Queue(T entity) {
		CacheObject<T> cacheObject = this.get(clazz, entity.getId());
		if (cacheObject != null) {
			UpdateAction updateAction = UpdateAction.valueOf(cacheObject, UpdateType.UPDATE);
			dbPersistService.handlerPersist(updateAction);
		}
	}


	@Override
	public void submitDeleted2Queue(T entity) {
		submitDeleted2Queue(entity.getId());
	}


	@Override
	public void submitDeleted2Queue(PK id) {
		CacheObject<T> cacheObject = this.get(clazz, id);
		if (cacheObject != null) {
			cacheObject.setUpdateStatus(UpdateStatus.DELETED);
			UpdateAction updateAction = UpdateAction.valueOf(cacheObject, UpdateType.DELETE);
			dbPersistService.handlerPersist(updateAction);
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


}
