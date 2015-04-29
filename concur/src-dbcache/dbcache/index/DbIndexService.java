package dbcache.index;

import dbcache.cache.CacheUnit;
import dbcache.IEntity;

import java.io.Serializable;
import java.util.Collection;


/**
 * 实体索引服务接口
 * <br/>框架自己维护
 * @author Jake
 * @date 2014年8月30日下午12:48:30
 */
public interface DbIndexService<PK extends Comparable<PK> & Serializable> {


	/**
	 * 获取索引值
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return Map.Entry<PK, Boolean>> 主键Id - 是否持久态(false:已删除)
	 */
	public Collection<PK> get(String indexName, Object indexValue);


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
	public void update(IEntity<PK> entity, String indexName, Object oldValue, Object newValue);


	/**
	 * 移除索引值
	 * @param indexValue 索引值
	 */
	public void remove(IndexValue<PK> indexValue);


	/**
	 * 获取缓存
	 * @return
	 */
	public CacheUnit getCacheUnit();


}
