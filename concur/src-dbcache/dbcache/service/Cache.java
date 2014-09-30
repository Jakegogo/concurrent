package dbcache.service;

import java.lang.ref.ReferenceQueue;


/**
 * 缓存容器接口
 * @author jake
 * @date 2014-7-31-下午8:31:25
 */
public interface Cache {

	/**
	 * 获取缓存包装对象
	 * @param key
	 * @return 为空表示未缓存,如果值为null,ValueWrapper.get()的值将为空
	 */
	ValueWrapper get(Object key);

	/**
	 * 存入缓存
	 * @param key 键
	 * @param value 值
	 */
	void put(Object key, Object value);

	/**
	 * 存入缓存并返回最新的值
	 * @param key 键
	 * @param value 值
	 * @return
	 */
	ValueWrapper putIfAbsent(Object key, Object value);

	/**
	 * 清除缓存
	 * @param key 对应的键
	 */
	void evict(Object key);

	/**
	 * 清空缓存
	 */
	void clear();

	/**
	 * 获取已经缓存对象的数量
	 * @return
	 */
	int getCachedSize();


	/**
	 * 缓存值包装接口
	 */
	interface ValueWrapper {

		/**
		 * 获取缓存值
		 */
		Object get();
	}


	/**
	 * 初始化
	 * @param entityCacheSize 缓存上限大小
	 * @param concurrencyLevel 并发线程数
	 */
	void init(int entityCacheSize, int concurrencyLevel);


	/**
	 * 获取回收队列
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	ReferenceQueue getReferencequeue();

}
