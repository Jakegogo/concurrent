package dbcache.cache.impl;

import dbcache.anno.ThreadSafe;

/**
 * 缓存移除监听接口
 * @author Jake
 * @date 2014年9月28日下午10:32:33
 */
@ThreadSafe
public interface EvictionListener<K, V> {

  /**
   * A call-back notification that the entry was evicted.
   *
   * @param key the entry's key
   * @param value the entry's value
   */
  void onEviction(K key, V value);
}