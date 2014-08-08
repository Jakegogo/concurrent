package dbcache.model;

/**
 * 实体标识接口，用于告知锁创建器具体类实例是实体对象
 * @author frank
 */
public interface IEntity<T extends Comparable<?>> {

	/**
	 * 获取实体标识
	 * @return
	 */
	T getIdentity();
	
}
