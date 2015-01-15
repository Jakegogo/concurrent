package dbcache.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import dbcache.support.jdbc.JdbcSupport;
import dbcache.support.jdbc.ModelInfo;
import dbcache.support.jdbc.RowMapper;

/**
 * Jdbc工具类
 * Created by Jake on 2015/1/10.
 */
@Component
public class JdbcUtil implements ApplicationContextAware {

	private static JdbcSupport jdbcSupport;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		jdbcSupport = applicationContext.getBean(JdbcSupport.class);
	}
	
	/**
     * 根据Id获取实体
     * @param clzz 实体类
     * @param id 主键
     * @return
     */
    public static <T> T get(final Class<T> clzz, Object id) {
    	return jdbcSupport.get(clzz, id);
    }
    
    
    /**
     * 保存实体
     * @param entity 实体对象
     */
    public static boolean save(Object entity) {
    	return jdbcSupport.save(entity);
    }
    
    
    /**
     * 更新实体
     * @param entity 实体对象
     * @return
     */
    public static boolean update(Object entity) {
    	return jdbcSupport.update(entity);
    }
    
    
    /**
     * 删除实体
     * @param entity 实体
     * @return
     */
    public static boolean delete(Object entity) {
    	return jdbcSupport.delete(entity);
    }
    
    
    /**
     * 根据属性查询实体列表
     * @param clzz 实体类
     * @param attrName 属性名
     * @param attrValue 属性值
     * @return
     */
	public static <T> List<T> listByAttr(final Class<T> clzz, String attrName, Object attrValue) {
    	return jdbcSupport.listByAttr(clzz, attrName, attrValue);
    }
    
    
    /**
     * 根据属性查询实体Id列表
     * @param clzz 实体类
     * @param attrName 属性名
     * @param attrValue 属性值
     * @return
     */
	public static List<?> listIdByAttr(final Class<?> clzz, String attrName, Object attrValue) {
    	return jdbcSupport.listIdByAttr(clzz, attrName, attrValue);
    }
    
	
	/**
     * 根据Sql查询实体列表
     * @param clzz 实体类
     * @param sql SQL语句
     * @param params 参数列表
     * @param <T> 类泛型
     * @return
     */
	public static <T> List<T> listEntityBySql(final Class<T> clzz, String sql, Object... params) {
    	return jdbcSupport.listEntityBySql(clzz, sql, params);
    }
    
    
    /**
     * 根据Sql查询对象列表
     * @param clzz 实体类
     * @param sql SQL语句
     * @param params 参数列表
     * @param <T> 类泛型
     * @return
     */
    public static <T> List<T> listBySql(final Class<T> clzz, String sql, Object... params) {
    	return jdbcSupport.listBySql(clzz, sql, params);
    }
    
    
    /**
     * 根据Sql查询对象列表
     * @param clzz 实体类
     * @param sql SQL语句
     * @param params 参数列表
     * @param <T> 类泛型
     * @return
     */
    public static <T> List<T> listBySql(String sql, RowMapper<T> rowMapper, Object... params) {
    	return jdbcSupport.listBySql(sql, rowMapper, params);
    }
	

}
