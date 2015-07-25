package dbcache.persist.service;

import dbcache.dbaccess.DbAccessService;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 批量数据库管理器接口
 */
public interface DbBatchAccessService extends DbAccessService {


	/**
	 * 批量保存实体对象
	 * @param clzz 实体类
	 * @param entitys 实体对象集合
	 */
	public void save(Class<?> clzz, Collection<Object> entitys);

	/**
	 * 批量更新实体对象
	 * @param clzz 实体类
	 * @param entitys 实体对象集合
	 */
	public void update(Class<?> clzz, Collection<Object> entitys);

	/**
	 * 动态更新实体对象
	 * @param entity 实体
	 * @param modifiedFields 修改过的属性集合(线程安全)
	 */
	public <T> void update(T entity, AtomicIntegerArray modifiedFields);
	
	/**
	 * 批量删除实体对象
	 * @param clzz 实体类
	 * @param entitys 实体对象集合
	 */
	public void delete(Class<?> clzz, Collection<Object> entitys);



}
