package dbcache.service.impl;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dbcache.model.WeakCacheEntity;
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
	 * 缓存容器
	 */
	@SuppressWarnings("rawtypes")
	private ConcurrentMap<Object, WeakReference> store;

	/**
	 * 回收队列
	 */
	private final FinalizableReferenceQueue referenceQueue = new FinalizableReferenceQueue("ConcurrentWeekHashMapCache.FinalizableReferenceQueue");


	@Autowired
	private ConfigFactory configFactory;


	/**
	 * 初始化
	 * @param entityCacheSize 初始缓存容量
	 * @param concurrencyLevel 并发值
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void init(int entityCacheSize, int concurrencyLevel) {
		this.store = new ConcurrentHashMap<Object, WeakReference>(DEFAULT_CAPACITY_OF_ENTITY_CACHE, 0.7f, concurrencyLevel);
	}


	/**
	 * 构造方法
	 */
	public ConcurrentWeekHashMapCache() {
	}


	@SuppressWarnings("rawtypes")
	@Override
	public ValueWrapper get(Object key) {
		WeakReference value = this.store.get(key);
		if(value == null && this.store.containsKey(key)) {
			return NULL_HOLDER;
		}
		if(value.get() == null) {
			return null;
		}
		ValueWrapper result = SimpleValueWrapper.valueOf(value);
		return result;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void put(Object key, Object value) {
		this.store.put(key, (WeakReference)value);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		return SimpleValueWrapper.valueOf(this.store.putIfAbsent(key, (WeakReference)value));
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
	public class FinalizableReferenceQueue extends ReferenceQueue<Object> {

		private final Object LOCK = new Object();

		private int i;

		/**
		 * 构造一个新的清除引用的引用队列。
		 */
		public FinalizableReferenceQueue() {
			synchronized (LOCK) {
				start("FinalizableReferenceQueue#" + ++i);
			}
		}

		/**
		 * 构造一个新的清除引用的引用队列。
		 * @param name 引用队列的名称，该名称用做清理的守护线程的名称。
		 */
		public FinalizableReferenceQueue(String name) {
			start(name);
		}

		/**
		 * 执行引用的清除工作。
		 * @param reference 要执行清除工作的引用。
		 */
		@SuppressWarnings("rawtypes")
		void cleanUp(Reference<?> reference) {
			try {
				store.remove(((WeakCacheEntity) reference).getKey());
			} catch (Throwable t) {
				logger.error("清除引用时发生错误", t);
			}
		}

		/**
		 * 开始垃圾回收引用监视。
		 */
		void start(String name) {
			Thread thread = new Thread(name) {

				@Override
				public void run() {
					while (true) {
						try {
							cleanUp(remove());
						} catch (InterruptedException e) {
							// 不处理
						}
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
			if (logger.isDebugEnabled()) {
				logger.debug("垃圾回收引用监视器[" + name + "]开始工作。");
			}
		}
	}


	@SuppressWarnings("serial")
	private static class NullHolder implements ValueWrapper, Serializable {

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
		public static SimpleValueWrapper valueOf(Object value) {
			if(value == null) {
				return null;
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


	@Override
	public FinalizableReferenceQueue getReferencequeue() {
		return referenceQueue;
	}


	@Override
	public int getCachedSize() {
		return store.size();
	}

}
