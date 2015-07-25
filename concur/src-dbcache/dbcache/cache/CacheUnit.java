package dbcache.cache;

import java.lang.ref.ReferenceQueue;


/**
 * 缓存单元接口
 * @author jake
 * @date 2014-7-31-下午8:31:25
 */
public interface CacheUnit {

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
	 * @retrun 之前的值
	 */
	ValueWrapper put(Object key, Object value);

	/**
	 * 存入缓存并返回最新的值
	 * @param key 键
	 * @param value 值
	 * @return 最新的值
	 */
	ValueWrapper putIfAbsent(Object key, Object value);

	/**
	 * 替换缓存内容
	 * @param key 键
	 * @param oldValue 旧值
	 * @param newValue 新值
	 * @return 最新的值
	 */
	ValueWrapper replace(Object key, Object oldValue, Object newValue);

	/**
	 * 清除缓存
	 * @param key 对应的键
	 * @return 移除的值
	 */
	ValueWrapper evict(Object key);

	/**
	 * 从缓存移除,如果外部依然保留引用,则会在引用释放且延迟移除
	 * @param key
	 * @return
	 */
	ValueWrapper remove(Object key);

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
	 * 获取缓存单元名称
	 * @return
	 */
	public String getName();

	/**
	 * 初始化
	 * @param name 缓存名称
	 * @param entityCacheSize 缓存上限大小
	 * @param concurrencyLevel 并发线程数
	 */
	void init(String name, int entityCacheSize, int concurrencyLevel);


	/**
	 * 获取回收队列
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	ReferenceQueue getReferencequeue();

}
