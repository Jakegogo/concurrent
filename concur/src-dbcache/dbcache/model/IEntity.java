package dbcache.model;

import java.io.Serializable;

/**
 * 实体标识接口，用于告知锁创建器具体类实例是实体对象
 * @author frank
 */
public interface IEntity<PK extends Comparable<PK> & Serializable> {

	/**
	 * 获取主键
	 * @return
	 */
	PK getId();
	
	
	/**
	 * 设置主键
	 * @param id
	 */
	void setId(PK id);
	
}
