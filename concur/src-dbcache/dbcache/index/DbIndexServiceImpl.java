package dbcache.index;

import dbcache.DbCacheInitError;
import dbcache.EnhancedEntity;
import dbcache.IEntity;
import dbcache.anno.ThreadSafe;
import dbcache.cache.CacheUnit;
import dbcache.cache.ValueWrapper;
import dbcache.conf.impl.CacheConfig;
import dbcache.conf.CacheRule;
import dbcache.conf.Inject;
import dbcache.dbaccess.DbAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import utils.collections.concurrent.ConcurrentHashMapV8;
import utils.enhance.asm.ValueGetter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private CacheUnit cacheUnit;

	@Autowired
	@Qualifier("jdbcDbAccessServiceImpl")
	private DbAccessService dbAccessService;
	
	/**
	 * 等待锁map {key:lock}
	 */
	private final ConcurrentMap<Object, Lock> WAITING_LOCK_MAP = new ConcurrentHashMapV8<Object, Lock>();


	@Override
	public Collection<PK> get(String indexName, Object indexValue) {
		
		if (cacheConfig == null) {
			throw new DbCacheInitError("CacheConfig未初始化,索引[" + indexName + "]!");
		}
		
		// 判断实体是否建立索引
		if (!cacheConfig.getIndexes().containsKey(indexName)) {
			throw new IllegalArgumentException("实体类["
					+ cacheConfig.getClazz().getSimpleName() 
					+ "]不存在索引["
					+ indexName + "]!");
		}
		
		final Map<PK, Boolean> indexValues = this.getPersist(indexName, indexValue).getIndexValues();
		// 索引为空
		if (indexValues == null) {
			return Collections.emptyList();
		}
		
		return new UnmodifiableKeySet<PK>(indexValues);
	}



	/**
	 * 获取可修改的线程安全的持久态索引值
	 * @return Map<PK, Boolean> 主键 - 是否持久化(false:已删除)
	 */
	@SuppressWarnings("unchecked")
	private IndexObject<PK> getPersist(String indexName, Object indexValue) {
		
		if (cacheConfig == null) {
			throw new RuntimeException("CacheConfig未初始化(" + indexName + ")");
		}

		final Object key = CacheRule.getIndexIdKey(indexName, indexValue);
		ValueWrapper wrapper = (ValueWrapper) cacheUnit.get(key);

		if (wrapper != null) {												// 已经缓存
			return (IndexObject<PK>) wrapper.get();
		}
		

		// 获取缓存唯一锁
		Lock lock = new ReentrantLock();
		Lock prevLock = WAITING_LOCK_MAP.putIfAbsent(key, lock);
		lock = prevLock != null ? prevLock : lock;
		
		// 查询数据库
		try {
			lock.lock();

			wrapper = (ValueWrapper) cacheUnit.get(key);
			if (wrapper != null) {												// 已经缓存
				return (IndexObject<PK>) wrapper.get();
			}

			// 查询数据库索引
			ValueGetter<?> indexField = cacheConfig.getIndexes().get(indexName);
			Collection<PK> entityIds = (Collection<PK>) dbAccessService
					.listIdByIndex(cacheConfig.getClazz(), indexField.getName(), indexValue);


			IndexObject<PK> indexObject = IndexObject.valueOf(IndexKey.valueOf(indexName, indexValue));
			if (entityIds != null) {
				ConcurrentMap<PK, Boolean> indexValues = indexObject.getIndexValues();
				// 需要外层加锁
				for (PK id : entityIds) {
					indexValues.putIfAbsent(id, true);
				}
			}
			
			wrapper = cacheUnit.putIfAbsent(key, indexObject);
		} finally {
			lock.unlock();
			WAITING_LOCK_MAP.remove(key);
		}

		return (IndexObject<PK>) wrapper.get();
	}


	/**
	 * 获取缓存中的索引Map
	 * @param indexObject IndexObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<PK, Boolean> getIndexMap(IndexObject<PK> indexObject) {
		if (indexObject == null) {
			return null;
		}
		return indexObject.getIndexValues();
	}


	@Override
	public void create(EnhancedEntity enhancedEntity, IndexValue<PK> indexValue) {

		IndexObject<PK> indexObject = this.getPersist(
				indexValue.getName(),
				indexValue.getValue())
				.put(indexValue.getId(), Boolean.valueOf(true));

		enhancedEntity.getRefHolder().addIndexObject(indexObject);

		// 索引变化监听
		if (cacheConfig.isHasIndexListeners()) {
			for (IndexChangeListener listener : cacheConfig.getIndexChangeListener()) {
				listener.onIndexCreate(indexValue.getName(), indexValue.getValue(), indexValue.getId());
			}
		}

	}


	@Override
	public void remove(EnhancedEntity enhancedEntity, IndexValue<PK> indexValue) {

		final Object key = CacheRule.getIndexIdKey(indexValue.getName(), indexValue.getValue());
		IndexObject<PK> indexObject = this.getPersist(
				indexValue.getName(),
				indexValue.getValue()).remove(key);

		enhancedEntity.getRefHolder().removeIndexObject(indexObject);

		// 索引变化监听
		if (cacheConfig.isHasIndexListeners()) {
			for (IndexChangeListener listener : cacheConfig.getIndexChangeListener()) {
				listener.onIndexRemove(indexValue.getName(), indexValue.getValue(), indexValue.getId());
			}
		}

	}


	@Override
	public void update(EnhancedEntity enhancedEntity, String indexName, Object oldValue, Object newValue) {
		if (oldValue != null && oldValue.equals(newValue)) {
			return;
		}

		IEntity<PK> entity = (IEntity<PK>) enhancedEntity.getEntity();


		// 从旧的索引队列中移除
		final Object key = CacheRule.getIndexIdKey(indexName, oldValue);
		IndexObject<PK> oldIndexObject = this.getPersist(indexName, oldValue).remove(key);

		enhancedEntity.getRefHolder().removeIndexObject(oldIndexObject);



		// 添加到新的索引队列
		IndexObject<PK> newIndexObject = this.getPersist(indexName, newValue)
				.put(entity.getId(), Boolean.valueOf(true));

		enhancedEntity.getRefHolder().addIndexObject(newIndexObject);


		// 索引变化监听
		if (cacheConfig.isHasIndexListeners()) {
			for (IndexChangeListener listener : cacheConfig.getIndexChangeListener()) {
				listener.onIndexChange(indexName, oldValue, newValue, entity.getId());
			}
		}
	}


	@Override
	public CacheUnit getCacheUnit() {
		return cacheUnit;
	}

	public CacheConfig<?> getCacheConfig() {
		return cacheConfig;
	}
	
	
	/**
	 * 不可更改的KeySet
	 * @author Jake
	 *
	 * @param <E>
	 */
	static class UnmodifiableKeySet<E> implements Collection<E>,
			Serializable {
		private static final long serialVersionUID = 5203572468382523850L;
		
		final Map<E, Boolean> c;

		UnmodifiableKeySet(Map<E, Boolean> c) {
			if (c == null)
				throw new NullPointerException();
			this.c = c;
		}

		public boolean contains(Object o) {
			Boolean val = c.get(o);
			return val != null && val.booleanValue();
		}

		public String toString() {
			return c.keySet().toString();
		}

		public Iterator<E> iterator() {
			return new Iterator<E>() {
				Map.Entry<E, Boolean> next = null;
				
				Iterator<Map.Entry<E, Boolean>> i = c.entrySet().iterator();

				public boolean hasNext() {
					Map.Entry<E, Boolean> next = null;
					while (i.hasNext() && 
							((next = i.next()) == null
									|| !next.getValue().booleanValue()));
					this.next = next;
					return next != null && next.getValue().booleanValue();
				}

				public E next() {
					return next.getKey();
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		public int size() {
			return c.size();
		}

		public boolean isEmpty() {
			return c.isEmpty();
		}
		
		public Object[] toArray() {
			return c.keySet().toArray();
		}

		public <T> T[] toArray(T[] a) {
			return c.keySet().toArray(a);
		}

		public boolean add(E e) {
			throw new UnsupportedOperationException();
		}

		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean containsAll(Collection<?> coll) {
			return c.keySet().containsAll(coll);
		}

		public boolean addAll(Collection<? extends E> coll) {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> coll) {
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> coll) {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			throw new UnsupportedOperationException();
		}
	}
	

}
