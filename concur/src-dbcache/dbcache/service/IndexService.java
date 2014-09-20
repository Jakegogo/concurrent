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
public interface IndexService<PK extends Comparable<PK> & Serializable> {


	/**
	 * 获取索引值
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 */
	public Collection<IndexValue<PK>> get(String indexName, Object indexValue);


	/**
	 * 获取索引值
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return
	 */
	public IndexValue<PK> getUnique(String indexName, Object indexValue);


	/**
	 * 创建实体索引
	 * @param indexValue 索引值
	 */
	public void create(IndexValue<PK> indexValue);


	/**
	 * 更新索引
	 * @param entity 实体
	 * @param indexName 索引名
	 * @param oldValue 旧字段值
	 * @param newValue 新字段值
	 */
	public void update(Object entity, String indexName, Object oldValue, Object newValue);


	/**
	 * 移除索引值
	 * @param indexValue 索引值
	 */
	public void remove(IndexValue<PK> indexValue);


	/**
	 * 获取缓存
	 * @return
	 */
	public Cache getCache();

}
