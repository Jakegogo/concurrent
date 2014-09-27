package dbcache.service.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dbcache.conf.CacheConfig;
import dbcache.conf.CacheRule;
import dbcache.conf.Inject;
import dbcache.model.IEntity;
import dbcache.model.IndexKey;
import dbcache.model.IndexObject;
import dbcache.model.IndexValue;
import dbcache.model.UpdateStatus;
import dbcache.refcache.ConcurrentReferenceMap;
import dbcache.refcache.ConcurrentReferenceMap.ReferenceKeyType;
import dbcache.refcache.ConcurrentReferenceMap.ReferenceValueType;
import dbcache.service.Cache;
import dbcache.service.DbAccessService;
import dbcache.service.IndexService;

/**
 * 实体索引服务实现类
 * <br/>更改实体索引值需要外部加锁
 * @author Jake
 * @date 2014年8月30日下午12:49:40
 */
@Component
public class IndexServiceImpl<PK extends Comparable<PK> & Serializable>
		implements IndexService<PK> {


	/**
	 * 实体缓存配置
	 */
	@Inject
	private CacheConfig cacheConfig;

	@Inject
	@Autowired
	@Qualifier("concurrentWeekHashMapCache")
	private Cache cache;

	@Autowired
	private DbAccessService dbAccessService;

	/**
	 * 等待锁map {key:lock}
	 */
	private final ConcurrentMap<Object, ReadWriteLock> WAITING_LOCK_MAP = new ConcurrentHashMap<Object, ReadWriteLock>();


	@Override
	public Collection<PK> get(String indexName, Object indexValue) {

		//判断实体是否建立索引
		if(!cacheConfig.getIndexes().containsKey(indexName)) {
			throw new IllegalArgumentException("实体类[" + cacheConfig.getClass().getSimpleName() + "]不存在索引[" + indexName + "]!");
		}

		Map<PK, Boolean> indexValues = getPersist(indexName, indexValue);

		if(indexValues == null) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableCollection(indexValues.keySet());
	}



	/**
	 * 获取可修改的线程安全的持久态索引值
	 * @return Map<PK, Boolean> 主键 - 是否持久化(false:已删除)
	 */
	@SuppressWarnings("unchecked")
	private Map<PK, Boolean> getPersist(String indexName, Object indexValue) {
		Object key = CacheRule.getIndexIdKey(indexName, indexValue);

		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		if(wrapper != null) {	// 已经缓存

			IndexObject<PK> indexObject = (IndexObject<PK>) wrapper.get();

			if(indexObject != null) {
				//持久态则返回结果
				if(indexObject.getUpdateStatus() == UpdateStatus.PERSIST) {
					return indexObject.getIndexValues();
				}

			} else {
				return null;
			}
		}


		ReadWriteLock lock = this.getIndexReadWriteLock(key);

		Map<PK, Boolean> indexValues = null;
		lock.writeLock().lock();
		try {

			IndexObject<PK> indexObject = null;
			wrapper = (Cache.ValueWrapper) cache.get(key);

			if(wrapper != null) {	// 已经缓存

				indexObject = (IndexObject<PK>) wrapper.get();

				if(indexObject != null) {
					//持久态直接返回结果
					if(indexObject.getUpdateStatus() == UpdateStatus.PERSIST) {
						return indexObject.getIndexValues();
					}

					indexValues = indexObject.getIndexValues();

				} else {
					return null;
				}
			}

			//初始化索引
			if(wrapper == null) {

				indexObject = IndexObject.valueOf(IndexKey.valueOf(indexName, indexValue));

				wrapper = cache.putIfAbsent(key, indexObject);

				if (wrapper != null && wrapper.get() != null) {
					indexObject = (IndexObject<PK>) wrapper.get();
				}

				indexValues = indexObject.getIndexValues();
			}

			//查询数据库索引
			Field indexField = cacheConfig.getIndexes().get(indexName);

			Collection<PK> ids = (Collection<PK>) dbAccessService.listIdByIndex(cacheConfig.getClass(), indexField.getName(), indexValue);

			if(ids != null) {
				for(PK id : ids) {
					Boolean oldStatus = indexValues.putIfAbsent(id, true);
					if(!oldStatus) {
						indexValues.remove(id);
					}
				}
			}

			//设置缓存状态
			indexObject.setUpdateStatus(UpdateStatus.PERSIST);

		} finally {
			lock.writeLock().unlock();
		}

		return indexValues;
	}



	/**
	 * 获取可修改的线程安全的临时索引值
	 * @return Map<PK, Boolean> 主键 - 是否持久化(false:已删除)
	 */
	@SuppressWarnings("unchecked")
	private IndexObject<PK> getTransient(String indexName, Object indexValue) {
		Object key = CacheRule.getIndexIdKey(indexName, indexValue);

		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		if(wrapper != null) {	// 已经缓存

			IndexObject<PK> indexObject = (IndexObject<PK>) wrapper.get();

			if(indexObject != null) {
				return indexObject;
			} else {
				return null;
			}
		}

		IndexObject<PK> indexObject = IndexObject.valueOf(IndexKey.valueOf(indexName, indexValue));
		//设置缓存状态
		indexObject.setUpdateStatus(UpdateStatus.TRANSIENT);

		wrapper = cache.putIfAbsent(key, indexObject);

		if (wrapper != null && wrapper.get() != null) {
			indexObject = (IndexObject<PK>) wrapper.get();
		}

		return indexObject;
	}


	/**
	 * 获取索引读写锁
	 * @param key 键
	 * @return
	 */
	private ReadWriteLock getIndexReadWriteLock(Object key) {
		ReadWriteLock lock = WAITING_LOCK_MAP.get(key);
		if(lock != null) {
			return lock;
		}

		lock = new ReentrantReadWriteLock();
		ReadWriteLock prevLock = WAITING_LOCK_MAP.putIfAbsent(key, lock);
		lock = prevLock != null ? prevLock : lock;
		return lock;
	}


	@Override
	public void create(IndexValue<PK> indexValue) {
		IndexObject<PK> indexObject = this.getTransient(indexValue.getName(), indexValue.getValue());
		if(indexObject.getUpdateStatus() == UpdateStatus.PERSIST) {
			indexObject.getIndexValues().put(indexValue.getId(), Boolean.valueOf(true));
		} else {
			Object key = CacheRule.getIndexIdKey(indexValue.getName(), indexValue.getValue());
			//持有读锁
			ReadWriteLock lock = this.getIndexReadWriteLock(key);
			lock.readLock().lock();
			try {
				indexObject.getIndexValues().put(indexValue.getId(), Boolean.valueOf(true));
			} finally {
				lock.readLock().unlock();
			}
		}
	}


	@Override
	public void remove(IndexValue<PK> indexValue) {
		IndexObject<PK> indexObject = this.getTransient(indexValue.getName(), indexValue.getValue());
		Map<PK, Boolean> indexValues = indexObject.getIndexValues();
		if(indexObject.getUpdateStatus() == UpdateStatus.PERSIST) {
			indexValues.remove(indexValue.getId());
		} else {
			Object key = CacheRule.getIndexIdKey(indexValue.getName(), indexValue.getValue());
			//持有读锁
			ReadWriteLock lock = this.getIndexReadWriteLock(key);
			lock.readLock().lock();
			try {
				indexValues.put(indexValue.getId(), Boolean.valueOf(false));
			} finally {
				lock.readLock().unlock();
			}
		}
	}


	@Override
	public void update(IEntity<PK> entity, String indexName, Object oldValue, Object newValue) {
		IndexObject<PK> oldIndexObject = this.getTransient(indexName, oldValue);
		Map<PK, Boolean> oldIndexValues = oldIndexObject.getIndexValues();
		if(oldIndexObject.getUpdateStatus() == UpdateStatus.PERSIST) {
			oldIndexValues.remove(entity.getId());
		} else {
			Object key = CacheRule.getIndexIdKey(indexName, oldValue);
			//持有读锁
			ReadWriteLock lock = this.getIndexReadWriteLock(key);
			lock.readLock().lock();
			try {
				oldIndexValues.put(entity.getId(), Boolean.valueOf(false));
			} finally {
				lock.readLock().unlock();
			}

		}


		IndexObject<PK> newIndexObject = this.getTransient(indexName, newValue);
		Map<PK, Boolean> newIndexValues = newIndexObject.getIndexValues();
		if(newIndexObject.getUpdateStatus() == UpdateStatus.PERSIST) {
			newIndexValues.remove(entity.getId());
		} else {
			Object key = CacheRule.getIndexIdKey(indexName, newValue);
			//持有读锁
			ReadWriteLock lock = this.getIndexReadWriteLock(key);
			lock.readLock().lock();
			try {
				newIndexValues.put(entity.getId(), Boolean.valueOf(false));
			} finally {
				lock.readLock().unlock();
			}
		}
	}


	@Override
	public Cache getCache() {
		return cache;
	}

	public CacheConfig getCacheConfig() {
		return cacheConfig;
	}

}
