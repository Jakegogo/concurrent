package dbcache.service.impl;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dbcache.conf.CacheConfig;
import dbcache.conf.CacheRule;
import dbcache.conf.Inject;
import dbcache.model.IndexValue;
import dbcache.service.Cache;
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
	@Qualifier("concurrentLinkedHashMapCache")
	private Cache cache;


	@Override
	public Collection<IndexValue<PK>> get(String indexName, Object indexValue) {
		Object key = CacheRule.getIndexIdKey(indexName, indexValue);

		Cache.ValueWrapper wrapper = (Cache.ValueWrapper) cache.get(key);
		if(wrapper != null) {	// 已经缓存
			@SuppressWarnings("unchecked")
			Collection<IndexValue<PK>> indexValues = (Collection<IndexValue<PK>>) wrapper.get();
			return indexValues;
		}




		return null;
	}

	@Override
	public IndexValue<PK> getUnique(String indexName, Object indexValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(IndexValue<PK> indexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(IndexValue<PK> indexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Object entity, String indexName, Object oldValue, Object newValue) {

	}


	@Override
	public Cache getCache() {
		return cache;
	}

	public CacheConfig getCacheConfig() {
		return cacheConfig;
	}

}
