package dbcache;

import java.io.Serializable;

/**
 * 实体标识接口
 * <br/>所有实体必须实现此接口
 * <br/>可告知锁创建器具体类实例是实体对象
 * @author jake
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
