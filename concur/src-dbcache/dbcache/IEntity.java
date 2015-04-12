package dbcache;

import java.io.Serializable;

/**
 * 实体(Entity)类统一接口
 * <br/>所有实体必须实现此接口
 * <br/>getId()方法回去唯一标识可用于锁链
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
