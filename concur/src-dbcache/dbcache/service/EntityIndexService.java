package dbcache.service;

import java.io.Serializable;
import java.util.Collection;

import dbcache.model.IndexKey;
import dbcache.model.IndexValue;


/**
 * 实体索引服务接口
 * @author Jake
 * @date 2014年8月30日下午12:48:30
 */
public interface EntityIndexService<PK extends Comparable<PK> & Serializable> {


	/**
	 * 获取索引值
	 * @param indexKey 索引Key
	 */
	public Collection<IndexValue<PK>> get(IndexKey<PK> indexKey);


	/**
	 * 获取索引值
	 * @param indexKey 索引Key
	 * @return
	 */
	public IndexValue<PK> getUnique(IndexKey<PK> indexKey);


	/**
	 * 创建实体索引
	 * @param indexKey 索引Key
	 * @param indexValue 索引值
	 */
	public void create(IndexValue<PK> indexValue);


	/**
	 * 更新索引
	 * @param indexKey 索引Key
	 * @param oldIndexValue 旧的索引值
	 * @param indexValue 新的索引值
	 */
	public void update(IndexValue<PK> oldIndexValue, IndexValue<PK> indexValue);
	
	
	/**
	 * 更新索引
	 * @param class 实体类
	 * @param indexName 索引名
	 * @param oldValue 旧字段值
	 * @param newValue 新字段值
	 */
	public void update(Class<? extends Object> class1, String indexName, Object oldValue, Object newValue);
	
	
	/**
	 * 移除索引值
	 * @param indexValue 索引值
	 */
	public void remove(IndexValue<PK> indexValue);
	
	
	
}
