package dbcache.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dbcache.annotation.ThreadSafe;
import dbcache.conf.CacheConfig;
import dbcache.conf.CacheRule;
import dbcache.conf.Inject;
import dbcache.model.IEntity;
import dbcache.model.IndexKey;
import dbcache.model.IndexObject;
import dbcache.model.IndexValue;
import dbcache.model.PersistStatus;
import dbcache.service.Cache;
import dbcache.service.DbAccessService;
import dbcache.service.DbIndexService;
import dbcache.support.asm.ValueGetter;

/**
 * 实体索引服务实现类
 * <br/>更改实体索引值需要外部加锁
 * @author Jake
 * @date 2014年8月30日下午12:49:40
 */
@ThreadSafe
@Component
public class DbIndexServiceImpl<PK extends Comparable<PK> & Serializable>
		implements DbIndexService<PK> {


	/**
	 * 实体缓存配置
	 */
	@Inject
	private CacheConfig<?> cacheConfig;

	@Inject
	@Autowired
	@Qualifier("concurrentLruHashMapCache")
	private Cache cache;

	@Autowired
	@Qualifier("jdbcDbAccessServiceImpl")
	private DbAccessService dbAccessService;


	@Override
	public Collection<PK> get(String indexName, Object indexValue) {

		// 判断实体是否建立索引
		if(!cacheConfig.getIndexes().containsKey(indexName)) {
			throw new IllegalArgumentException("实体类[" + cacheConfig.getClass().getSimpleName() + "]不存在索引[" + indexName + "]!");
		}

		final Map<PK, Boolean> indexValues = this.getPersist(indexName, indexValue);
		// 索引为空
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

		IndexObject<PK> indexObject = this.getTransient(indexName, indexValue);

		if(indexObject == null) {
			return null;
		} else if(indexObject.getUpdateStatus() == PersistStatus.PERSIST) {
			return indexObject.getIndexValues();
		}

		final ReadWriteLock lock = indexObject.getLock();

		ConcurrentMap<PK, Boolean> indexValues = null;
		lock.writeLock().lock();
		try {

			// 持久态则返回结果
			if(indexObject.getUpdateStatus() == PersistStatus.PERSIST) {
				return indexObject.getIndexValues();
			}

			indexValues = indexObject.getIndexValues();

			// 查询数据库索引
			ValueGetter<?> indexField = cacheConfig.getIndexes().get(indexName);

			Collection<PK> ids = (Collection<PK>) dbAccessService.listIdByIndex(cacheConfig.getClazz(), indexField.getName(), indexValue);

			if(ids != null) {
				// 需要外层加锁
				for(PK id : ids) {
					Boolean oldStatus = indexValues.putIfAbsent(id, true);
					if(oldStatus != null && !oldStatus) {
						indexValues.remove(id);
					}
				}
			}

			// 设置缓存状态
			indexObject.setUpdateStatus(PersistStatus.PERSIST);
			// 清除锁
			indexObject.setLock(null);

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
		final Object key = CacheRule.getIndexIdKey(indexName, indexValue);

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
		// 设置缓存状态
		indexObject.setUpdateStatus(PersistStatus.TRANSIENT);

		wrapper = cache.putIfAbsent(key, indexObject);

		if (wrapper != null && wrapper.get() != null) {
			indexObject = (IndexObject<PK>) wrapper.get();
		}

		return indexObject;
	}


	@Override
	public IndexObject<PK> create(IndexValue<PK> indexValue) {

		final IndexObject<PK> indexObject = this.getTransient(indexValue.getName(), indexValue.getValue());

		// 持久状态
		if(indexObject.getUpdateStatus() == PersistStatus.PERSIST) {

			indexObject.getIndexValues().put(indexValue.getId(), Boolean.valueOf(true));

			return this.getTransient(indexValue.getName(), indexValue.getValue());
		} else {// 内存临时状态
			// 持有读锁
			final ReadWriteLock lock = indexObject.getLock();
			lock.readLock().lock();
			try {

				indexObject.getIndexValues().put(indexValue.getId(), Boolean.valueOf(true));

				return this.getTransient(indexValue.getName(), indexValue.getValue());
			} finally {
				lock.readLock().unlock();
			}
		}

	}


	@Override
	public void remove(IndexValue<PK> indexValue) {

		final Object key = CacheRule.getIndexIdKey(indexValue.getName(), indexValue.getValue());

		final IndexObject<PK> indexObject = this.getTransient(indexValue.getName(), indexValue.getValue());

		// 持久状态直接移除
		if(indexObject.getUpdateStatus() == PersistStatus.PERSIST) {
			indexObject.getIndexValues().remove(key);
		} else {//内存临时虚存储状态
			// 持有读锁
			final ReadWriteLock lock = indexObject.getLock();

			lock.readLock().lock();
			try {
				indexObject.getIndexValues().put(indexValue.getId(), Boolean.valueOf(false));
			} finally {
				lock.readLock().unlock();
			}
		}

	}


	@Override
	public void update(IEntity<PK> entity, String indexName, Object oldValue, Object newValue) {
		if(oldValue != null && oldValue.equals(newValue)) {
			return;
		}
		// 从旧的索引队列中移除
		this.remove(IndexValue.valueOf(indexName, oldValue, entity.getId()));
		// 添加到新的索引队列
		this.create(IndexValue.valueOf(indexName, newValue, entity.getId()));
	}


	@Override
	public Cache getCache() {
		return cache;
	}

	public CacheConfig<?> getCacheConfig() {
		return cacheConfig;
	}

}
