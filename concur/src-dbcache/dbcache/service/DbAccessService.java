package dbcache.service;

import java.io.Serializable;
import java.util.Collection;

/**
 * 通用数据库管理器接口
 */
public interface DbAccessService {


	/**
	 * 根据主键id取得实体对象
	 *
	 * @param entityClazz
	 *            实体类
	 * @param id
	 *            主键id
	 * @return 实体对象
	 */
	<T> T get(Class<T> entityClazz, Serializable id);


	/**
	 * 保存实体对象
	 *
	 * @param entity
	 *            实体对象
	 */
	<T> void save(T entity);


	/**
	 * 更新实体对象
	 *
	 * @param entity
	 *            实体对象
	 */
	<T> void update(T entity);


	/**
	 * 删除实体
	 *
	 * @param entity
	 *            实体对象
	 */
	<T> void delete(T entity);


	/**
	 * 取得最大主键值(主键为Integer/Long类型)
	 * @param entityClazz 实体类
	 * @return Object
	 */
	Object loadMaxId(Class<?> entityClazz);


	/**
	 * 取得最大主键值(主键为Long类型)
	 * @param clz 实体类
	 * @param minValue 范围-最小值
	 * @param maxValue 范围-最大值
	 * @return
	 */
	Object loadMaxId(Class<?> entityClazz, long minValue, long maxValue);


	/**
	 * 根据索引获取Id列表
	 * @param entityClazz 实体类
	 * @param fieldName 属性名
	 * @param indexValue 索引值
	 * @return
	 */
	Collection<?> listIdByIndex(Class<?> entityClazz,
			String fieldName, Object indexValue);

}
