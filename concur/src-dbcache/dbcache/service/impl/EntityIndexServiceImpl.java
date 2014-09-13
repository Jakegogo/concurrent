package dbcache.service.impl;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dbcache.model.IndexKey;
import dbcache.model.IndexValue;
import dbcache.service.Cache;
import dbcache.service.EntityIndexService;

/**
 * 实体索引服务实现类
 * <br/>更改实体索引值需要外部加锁
 * @author Jake
 * @date 2014年8月30日下午12:49:40
 */
@Component
public class EntityIndexServiceImpl<PK extends Comparable<PK> & Serializable>
		implements EntityIndexService<PK> {


	@Autowired
	@Qualifier("concurrentLinkedHashMapCache")
	private Cache cache;


	@Override
	public Collection<IndexValue<PK>> get(IndexKey<PK> indexKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexValue<PK> getUnique(IndexKey<PK> indexKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(IndexValue<PK> indexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(IndexValue<PK> oldIndexValue, IndexValue<PK> indexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(IndexValue<PK> indexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Object entity, String indexName,
			Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
//		System.out.println("called changeIndex:" + entity.getClass().getName() + " - " + indexName + " - " + oldValue + " - " + newValue);
	}

}
