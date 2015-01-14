package dbcache.service.impl;

import dbcache.service.DbAccessService;
import dbcache.support.jdbc.JdbcSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * 数据库存储jdbc服务实现类
 * @author jake
 * @date 2014-7-31-下午8:18:16
 */
@Component("jdbcDbAccessServiceImpl")
public class JdbcDbAccessServiceImpl implements DbAccessService {


	@Autowired
	private JdbcSupport jdbcSupport;

	/**
	 * 根据主键id取得实体对象
	 * @param entityClazz 实体类
	 * @param id 主键id
	 * @return 实体对象
	 */
	@Override
	public <T> T get(Class<T> entityClazz, Serializable id) {
		return jdbcSupport.get(entityClazz, id);
	}


	/**
	 * 保存实体对象
	 * @param entity 实体对象
	 */
	@Override
	public <T> void save(T entity) {
		jdbcSupport.save(entity);
	}


	/**
	 * 更新实体对象
	 * @param entity 实体对象
	 */
	@Override
	public <T> void update(T entity) {
		jdbcSupport.update(entity);
	}


	/**
	 * 删除实体
	 * @param entity 实体对象
	 */
	@Override
	public <T> void delete(T entity) {
		if (entity != null) {
			jdbcSupport.delete(entity);
		}
	}


	/**
	 * 取得最大主键值(主键为Integer/Long类型)
	 * @param entityClazz 实体对象
	 * @return Object
	 */
	@Override
	public Object loadMaxId(Class<?> entityClazz) {
		return jdbcSupport.getMaxPrimaryKey(entityClazz, 0l, Long.MAX_VALUE);
	}


	/**
	 * 取得最大主键值(主键为Long类型)
	 * @param entityClazz 实体类
	 * @param minValue 范围-最小值
	 * @param maxValue 范围-最大值
	 * @return
	 */
	@Override
	public Object loadMaxId(Class<?> entityClazz, long minValue, long maxValue) {
		return jdbcSupport.getMaxPrimaryKey(entityClazz, minValue, maxValue);
	}


	/**
	 * 更加属性名和属性值获取ID列表
	 */
	@Override
	public Collection<?> listIdByIndex(
			Class<?> entityClazz, String fieldName,
			Object fieldValue) {
		return jdbcSupport.listIdByAttr(entityClazz, fieldName, fieldValue);
	}


}
