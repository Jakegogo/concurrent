package dbcache.service.impl;

import java.io.Serializable;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;

import dbcache.conf.CacheConfig;
import dbcache.service.DbAccessService;

/**
 * 数据库存储服务实现类
 * @author jake
 * @date 2014-7-31-下午8:18:16
 */
@Component("DbAccessServiceImpl")
public class DbAccessServiceImpl extends HibernateDaoSupport implements DbAccessService {


	@Autowired
    public void setSessionFactory0(SessionFactory sessionFactory){
    	super.setSessionFactory(sessionFactory);
    }


	/**
	 * 根据主键id取得实体对象
	 * @param entityClazz 实体类
	 * @param id 主键id
	 * @return 实体对象
	 */
	@Override
	public <T> T get(Class<T> entityClazz, Serializable id) {
		return super.getHibernateTemplate().get(entityClazz, id);
	}


	/**
	 * 保存实体对象
	 * @param entity 实体对象
	 */
	@Override
	public <T> void save(T entity) {
		super.getHibernateTemplate().saveOrUpdate(entity);
	}


	/**
	 * 更新实体对象
	 * @param entity 实体对象
	 */
	@Override
	public <T> void update(T entity) {
		super.getHibernateTemplate().update(entity);
	}


	/**
	 * 删除实体
	 * @param entityClazz 实体对象
	 * @param id 主键id
	 */
	@Override
	public <T> void delete(T entity) {
		if (entity != null) {
			super.getHibernateTemplate().delete(entity);
		}
	}


	/**
	 * 取得最大主键值(主键为Integer/Long类型)
	 * @param entityClazz 实体对象
	 * @return Object
	 */
	@Override
	public Object loadMaxId(Class<?> entityClazz) {
		return getSession().createCriteria(entityClazz)
				.setProjection(Projections.max(Projections.id().toString()))
				.uniqueResult();
	}


	/**
	 * 取得最大主键值(主键为Long类型)
	 * @param clz 实体对象
	 * @param minValue 范围-最小值
	 * @param maxValue 范围-最大值
	 * @return
	 */
	@Override
	public Object loadMaxId(Class<?> entityClazz, long minValue, long maxValue) {
		return getSession()
				.createCriteria(entityClazz)
				.add(Restrictions.between(Projections.id().toString(), minValue, maxValue))
				.setProjection(Projections.max(Projections.id().toString()))
				.uniqueResult();
	}


	/**
	 * 更加属性名和属性值获取ID列表
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Serializable> listIdByIndex(
			Class<? extends CacheConfig> entityClazz, String fieldName,
			Object fieldValue) {
		return getSession()
				.createCriteria(entityClazz)
				.add(Restrictions.eq(fieldName, fieldValue))
				.setProjection(Projections.id())
				.list();
	}

}
