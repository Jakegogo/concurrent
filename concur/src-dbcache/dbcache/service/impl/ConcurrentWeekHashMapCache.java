package dbcache.service.impl;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dbcache.model.WeakCacheEntity;
import dbcache.model.WeakCacheObject;
import dbcache.service.Cache;
import dbcache.service.ConfigFactory;

/**
 * ConcurrentWeekHashMap缓存容器
 *
 * @author jake
 * @date 2014-8-1-下午8:30:34
 */
@Component("concurrentWeekHashMapCache")
public class ConcurrentWeekHashMapCache implements Cache {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentWeekHashMapCache.class);

	/**
	 * 初始容量
	 */
	private static final int DEFAULT_CAPACITY_OF_ENTITY_CACHE = 1000;

	/**
	 * 空值的引用
	 */
	private static final ValueWrapper NULL_HOLDER = new NullHolder();

	/**
	 * 所有的回收队列
	 */
	@SuppressWarnings("rawtypes")
	private static final ConcurrentLinkedQueue<FinalizableReferenceQueue> referenceQueues = new ConcurrentLinkedQueue<FinalizableReferenceQueue>();

	/**
	 * 缓存容器
	 */
	@SuppressWarnings("rawtypes")
	private ConcurrentMap<Object, WeakCacheObject> store;

	/**
	 * 回收队列
	 */
	@SuppressWarnings("rawtypes")
	private final FinalizableReferenceQueue referenceQueue = new FinalizableReferenceQueue();


	@Autowired
	private ConfigFactory configFactory;


	static {

		Thread thread = new Thread("垃圾回收引用监视器") {

			@SuppressWarnings("rawtypes")
			@Override
			public void run() {

				long waitTimmer = TimeUnit.SECONDS.toMillis(1);
				while (true) {

					for(Iterator<FinalizableReferenceQueue> it = referenceQueues.iterator();it.hasNext();) {
						try {
							it.next().cleanUp(waitTimmer);
						} catch (Exception e) {
							// 不处理
						}
					}

					try {
						Thread.sleep(waitTimmer);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();

		if (logger.isDebugEnabled()) {
			logger.debug("垃圾回收引用监视器开始工作。");
		}

	}


	/**
	 * 初始化
	 * @param entityCacheSize 初始缓存容量
	 * @param concurrencyLevel 并发值
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void init(int entityCacheSize, int concurrencyLevel) {
		this.store = new ConcurrentHashMap<Object, WeakCacheObject>(DEFAULT_CAPACITY_OF_ENTITY_CACHE, 0.7f, concurrencyLevel);
		referenceQueues.add(this.referenceQueue);
	}


	/**
	 * 构造方法
	 */
	public ConcurrentWeekHashMapCache() {
	}


	@SuppressWarnings("rawtypes")
	@Override
	public ValueWrapper get(Object key) {
		WeakCacheObject value = this.store.get(key);
		if(value == null || value.getProxyEntity() == null) {
			if(this.store.containsKey(key)) {
				return NULL_HOLDER;
			}
			return null;
		}
		ValueWrapper result = SimpleValueWrapper.valueOf(value);
		return result;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void put(Object key, Object value) {
		this.store.put(key, (WeakCacheObject) value);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		return SimpleValueWrapper.valueOf(this.store.putIfAbsent(key, (WeakCacheObject) (value==null ? NULL_HOLDER : value)));
	}


	@Override
	public void evict(Object key) {
		this.store.remove(key);
	}


	@Override
	public void clear() {
		this.store.clear();
	}


	/**
	 * 用做 ReferenceMap 的清除引用的引用队列。
	 */
	public class FinalizableReferenceQueue<T> extends ReferenceQueue<T> {

		/**
		 * 构造一个新的清除引用的引用队列。
		 */
		public FinalizableReferenceQueue() {
		}


		/**
		 * 执行引用的清除工作。
		 * @param waitTimmer 清除等待时间
		 */
		@SuppressWarnings("rawtypes")
		void cleanUp(long waitTimmer) {
			try {
				Reference<? extends T> reference = null;
				do {
					reference = remove(waitTimmer);
					if(reference != null) {
						Object key = ((WeakCacheEntity) reference).getKey();
						WeakCacheObject value = store.get(key);
						if(value != null && value.getProxyEntity() == null) {
							store.remove(key);
						}
					}
				} while(reference != null);
			} catch (Throwable t) {
				logger.error("清除引用时发生错误", t);
			}
		}

	}


	@SuppressWarnings({ "serial", "rawtypes" })
	private static class NullHolder extends WeakCacheObject implements ValueWrapper, Serializable {

		@Override
		public Object get() {
			return null;
		}
	}

	/**
	 * 缓存Value简单包装
	 *
	 * @author jake
	 * @date 2014-7-31-下午8:29:49
	 */
	public static class SimpleValueWrapper implements ValueWrapper {

		/** 缓存的实体 */
		private final Object value;

		/**
		 * 构造方法
		 *
		 * @param value 实体(可以为空)
		 */
		public SimpleValueWrapper(Object value) {
			this.value = value;
		}

		/**
		 * 获取实例
		 * @param value 值
		 * @return
		 */
		public static ValueWrapper valueOf(Object value) {
			if(value == null) {
				return NULL_HOLDER;
			}
			return new SimpleValueWrapper(value);
		}

		/**
		 * 获取实体
		 */
		public Object get() {
			return this.value;
		}

	}


	@SuppressWarnings("rawtypes")
	@Override
	public FinalizableReferenceQueue getReferencequeue() {
		return referenceQueue;
	}


	@Override
	public int getCachedSize() {
		return store.size();
	}

}
