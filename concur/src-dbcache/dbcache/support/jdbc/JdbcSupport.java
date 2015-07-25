package dbcache.support.jdbc;

import dbcache.anno.Shard;
import dbcache.conf.ShardStrategy;
import dbcache.pkey.IdGenerator;
import utils.enhance.asm.util.AsmUtils;
import dbcache.utils.MutableInteger;
import utils.StringUtils;
import utils.collections.concurrent.IdentityHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Jdbc Dao支持
 * <br/> 支持部分javax.persistence注解
 * @see javax.persistence.Table
 * @see javax.persistence.Id
 * @see javax.persistence.Column
 * @see javax.persistence.Transient
 * @see javax.persistence.Entity
 * @see javax.persistence.MappedSuperclass
 * Created by Jake on 2015/1/10.
 */
@Component
public class JdbcSupport {

	@Autowired
    private Config config;

    /** 实体信息缓存 */
    private IdentityHashMap<Class<?>, ModelInfo> modelInfoCache = new IdentityHashMap<Class<?>, ModelInfo>();


    /**
     * 根据Id获取实体
     * @param clzz 实体类
     * @param id 主键
     * @return
     */
    @SuppressWarnings("unchecked")
	public <T> T get(final Class<T> clzz, Object id) {

    	ModelInfo modelInfo = getOrCreateModelInfo(clzz);
    	String sql = modelInfo.getOrCreateSelectSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, id);

			rs = pst.executeQuery();
			
			return (T) modelInfo.generateEntity(rs);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


    /**
     * 保存实体
     * @param entity 实体对象
     */
    public boolean save(Object entity) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	String saveSql = modelInfo.getOrCreateSaveSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(saveSql);

			Object[] params = modelInfo.getSaveParams(entity);
			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }
    
    
    /**
     * 批量保存实体
     * @param clzz 实体类
     * @param entitys 实体对象
     */
    public int[] batchSave(final Class<?> clzz, Collection<Object> entitys) {

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
    		
    		
		    conn = config.getConnection();
		    conn.setAutoCommit(false);
		    
		    ModelInfo modelInfo = getOrCreateModelInfo(clzz);
		    String updateSql = modelInfo.getOrCreateSaveSql(config.dialect);
		    	
			pst = conn.prepareStatement(updateSql);
			
			for (Iterator<Object> it = entitys.iterator(); it.hasNext();) {
				Object entity = it.next();
				Object[] params = modelInfo.getSaveParams(entity);
				config.dialect.fillStatement(pst, params);
				pst.addBatch();
			}
			
			return pst.executeBatch();
			
    	} catch (Exception e) {
			try {
				// 若出现异常，对数据库中所有已完成的操作全部撤销，则回滚到事务开始状态
				if (conn != null && !conn.isClosed()) {
					conn.rollback();// 4,当异常发生执行catch中SQLException时，记得要rollback(回滚)；
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new JdbcExecuteException(e1);
			}
			handleException(conn, e);
    	} finally {
    		try {
				if (conn != null && !conn.isClosed()) {
					conn.setAutoCommit(true);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
				throw new JdbcExecuteException(e2);
			}
    		
    		config.close(pst, conn);
    	}
    	return new int[0];
    }
    

    /**
     * 使用Id生成器保存实体
     * @param entity 实体对象
     */
    public boolean saveWithAutoId(Object entity) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	String saveSql = modelInfo.getOrCreateSaveSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(saveSql);

			Object[] params = modelInfo.getAutoIdSaveParams(entity);
			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }


    /**
     * 使用Id生成器保存实体
     * @param entity 实体对象
     * @param category 分段/类别
     */
    public boolean saveWithAutoId(Object entity, int category) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	String saveSql = modelInfo.getOrCreateSaveSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(saveSql);

			Object[] params = modelInfo.getAutoIdSaveParams(entity, category);
			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }


    /**
     * 更新实体
     * @param entity 实体对象
     * @return
     */
    public boolean update(Object entity) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	String updateSql = modelInfo.getOrCreateUpdateSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(updateSql);

			Object[] params = modelInfo.getUpdateParams(entity);
			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }
    
    
    /**
     * 更新实体
     * @param entity 实体对象
     * @param modifiedFields 修改过的属性集合
     * @return
     */
    public boolean update(Object entity, Collection<String> modifiedFields) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	
    	List<String> modifiedFieldList = new ArrayList<String>();
    	for (String field : modifiedFields) {
    		modifiedFieldList.add(field);
    	}
    	
    	String updateSql = modelInfo.getOrCreateUpdateSql(config.dialect, modifiedFieldList);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(updateSql);

			Object[] params = modelInfo.getUpdateParams(entity, modifiedFieldList);
			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }
    
    
    /**
     * 更新实体
     * @param entity 实体对象
     * @param modifiedFields 修改过的属性数组(线程安全)
     * @return
     */
    public boolean update(Object entity, AtomicIntegerArray modifiedFields) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	
    	int length = modifiedFields.length();
    	List<Integer> modifiedFieldList = new ArrayList<Integer>(length);
    	for (int i = 0;i < length;i ++) {
    		if (modifiedFields.get(i) == 1) {
    			modifiedFields.set(i, 0);
    			modifiedFieldList.add(Integer.valueOf(i));
    		}
    	}
    	
    	String updateSql = modelInfo.getOrCreateUpdateSql(modifiedFieldList, config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(updateSql);

			Object[] params = modelInfo.getUpdateParams(modifiedFieldList, entity);
			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }
    
    
    /**
     * 批量更新实体
     * @param entitys 实体对象
     */
    public void batchUpdate(Collection<Object> entitys) {
    	
    	Map<Class<?>, List<Object>> entityClassMap = new HashMap<Class<?>, List<Object>>();
    	
    	for (Object entity : entitys) {
    		List<Object> list = entityClassMap.get(entity.getClass());
    		if (list == null) {
    			list = new LinkedList<Object>();
    			entityClassMap.put(entity.getClass(), list);
    		}
    		list.add(entity);
    	}

    	Connection conn = null;
    	try {
    		
    		PreparedStatement pst = null;
		    conn = config.getConnection();
		    conn.setAutoCommit(false);
		    
		    for (Entry<Class<?>, List<Object>> entry : entityClassMap.entrySet()) {
	    		ModelInfo modelInfo = getOrCreateModelInfo(entry.getKey());
		    	String updateSql = modelInfo.getOrCreateUpdateSql(config.dialect);
		    	
				pst = conn.prepareStatement(updateSql);
				
				for (Iterator<Object> it = entry.getValue().iterator(); it.hasNext();) {
					Object entity = it.next();
					Object[] params = modelInfo.getUpdateParams(entity);
					config.dialect.fillStatement(pst, params);
					pst.addBatch();
				}
				pst.executeBatch();
				pst.close();
		    }
		    
		    conn.setAutoCommit(true);
    	} catch (Exception e) {
			try {
				// 若出现异常，对数据库中所有已完成的操作全部撤销，则回滚到事务开始状态
				if (conn != null && !conn.isClosed()) {
					conn.rollback();// 4,当异常发生执行catch中SQLException时，记得要rollback(回滚)；
					conn.setAutoCommit(true);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new JdbcExecuteException(e1);
			}
			handleException(conn, e);
    	} finally {
    		config.close(conn);
    	}
    }

    
    /**
     * 批量更新实体
     * @param clzz 实体类
     * @param entitys 实体对象
     */
    public int[] batchUpdate(final Class<?> clzz, Collection<Object> entitys) {

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
    		
    		
		    conn = config.getConnection();
		    conn.setAutoCommit(false);
		    
		    ModelInfo modelInfo = getOrCreateModelInfo(clzz);
		    String updateSql = modelInfo.getOrCreateUpdateSql(config.dialect);
		    	
			pst = conn.prepareStatement(updateSql);
			
			for (Iterator<Object> it = entitys.iterator(); it.hasNext();) {
				Object entity = it.next();
				Object[] params = modelInfo.getUpdateParams(entity);
				config.dialect.fillStatement(pst, params);
				pst.addBatch();
			}
			
			return pst.executeBatch();
			
    	} catch (Exception e) {
			try {
				// 若出现异常，对数据库中所有已完成的操作全部撤销，则回滚到事务开始状态
				if (conn != null && !conn.isClosed()) {
					conn.rollback();// 4,当异常发生执行catch中SQLException时，记得要rollback(回滚)；
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new JdbcExecuteException(e1);
			}
			handleException(conn, e);
    	} finally {
    		try {
				if (conn != null && !conn.isClosed()) {
					conn.setAutoCommit(true);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
				throw new JdbcExecuteException(e2);
			}
    		
    		config.close(pst, conn);
    	}
    	return new int[0];
    }
    
    

    /**
     * 删除实体
     * @param entity 实体
     * @return
     */
    public boolean delete(Object entity) {
    	ModelInfo modelInfo = getOrCreateModelInfo(entity.getClass());
    	String deleteSql = modelInfo.getOrCreateDeleteSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(deleteSql);

			Object param = modelInfo.getDeleteParam(entity);
			config.dialect.fillStatement(pst, param);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
    	return false;
    }
    
    
    /**
     * 批量删除实体
     * @param clzz 实体类
     * @param entitys 实体对象
     */
    public int[] batchDelete(final Class<?> clzz, Collection<Object> entitys) {

    	Connection conn = null;
    	PreparedStatement pst = null;
    	try {
    		
    		
		    conn = config.getConnection();
		    conn.setAutoCommit(false);
		    
		    ModelInfo modelInfo = getOrCreateModelInfo(clzz);
		    String updateSql = modelInfo.getOrCreateDeleteSql(config.dialect);
		    	
			pst = conn.prepareStatement(updateSql);
			
			for (Iterator<Object> it = entitys.iterator(); it.hasNext();) {
				Object entity = it.next();
				Object params = modelInfo.getDeleteParam(entity);
				config.dialect.fillStatement(pst, params);
				pst.addBatch();
			}
			
			return pst.executeBatch();
			
    	} catch (Exception e) {
			try {
				// 若出现异常，对数据库中所有已完成的操作全部撤销，则回滚到事务开始状态
				if (conn != null && !conn.isClosed()) {
					conn.rollback();// 4,当异常发生执行catch中SQLException时，记得要rollback(回滚)；
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new JdbcExecuteException(e1);
			}
			handleException(conn, e);
    	} finally {
    		try {
				if (conn != null && !conn.isClosed()) {
					conn.setAutoCommit(true);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
				throw new JdbcExecuteException(e2);
			}
    		
    		config.close(pst, conn);
    	}
    	return new int[0];
    }


    /**
     * 根据属性查询实体列表
     * @param clzz 实体类
     * @param attrName 属性名
     * @param attrValue 属性值
     * @return
     */
    @SuppressWarnings("unchecked")
	public <T> List<T> listByAttr(final Class<T> clzz, String attrName, Object attrValue) {
    	ModelInfo modelInfo = getOrCreateModelInfo(clzz);
    	String sql = modelInfo.getOrCreateFindByAttributeSql(config.dialect, attrName);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, attrValue);

			rs = pst.executeQuery();

			return (List<T>) modelInfo.generateEntityList(rs);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


    /**
     * 根据属性查询实体Id列表
     * @param clzz 实体类
     * @param attrName 属性名
     * @param attrValue 属性值
     * @return
     */
    public List<?> listIdByAttr(final Class<?> clzz, String attrName, Object attrValue) {
    	ModelInfo modelInfo = getOrCreateModelInfo(clzz);
    	String sql = modelInfo.getOrCreateFindIdByAttributeSql(config.dialect, attrName);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, attrValue);

			rs = pst.executeQuery();

			return modelInfo.generateIdList(rs);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


    /**
     * 获取范围内最大的主键值
     * @param clzz 实体类
     * @param minValue 范围-下限
     * @param maxValue 范围-上限
     * @return
     */
    public Object getMaxPrimaryKey(final Class<?> clzz, Object minValue, Object maxValue) {
    	ModelInfo modelInfo = getOrCreateModelInfo(clzz);
    	String sql = modelInfo.getOrCreateSelectMaxIdSql(config.dialect);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, minValue, maxValue);

			rs = pst.executeQuery();

			return modelInfo.generateUniqueResult(rs);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


    /**
     * 根据Sql查询实体列表
     * @param clzz 实体类
     * @param sql SQL语句
     * @param params 参数列表
     * @param <T> 类泛型
     * @return
     */
    @SuppressWarnings("unchecked")
	public <T> List<T> listEntityBySql(final Class<T> clzz, String sql, Object... params) {
    	ModelInfo modelInfo = getOrCreateModelInfo(clzz);

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, params);

			rs = pst.executeQuery();

			return (List<T>) modelInfo.generateEntityList(rs);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


    /**
     * 根据Sql查询对象列表
     * @param clzz 查询结果类型
     * @param sql SQL语句
     * @param params 参数列表
     * @param <T> 类泛型
     * @return
     */
    public <T> List<T> listBySql(final Class<T> clzz, String sql, Object... params) {
    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;

    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, params);

			rs = pst.executeQuery();

			return (List<T>) this.generateObjectList(rs, clzz);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


    /**
     * 根据Sql查询对象列表
     * @param sql SQL语句
     * @param rowMapper 行映射
     * @param params 参数列表
     * @param <T> 类泛型
     * @return
     */
    public <T> List<T> listBySql(String sql, RowMapper<T> rowMapper, Object... params) {

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();

			pst = conn.prepareStatement(sql);
			config.dialect.fillStatement(pst, params);

			rs = pst.executeQuery();

			return this.generateObjectList(rs, rowMapper);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
    	return null;
    }


	/**
	 * 执行sql语句
	 * @param sql SQL语句
	 * @param params 参数列表
	 * @return
	 */
	public boolean executeQuery(String sql, Object... params) {

		Connection conn = null;
		PreparedStatement pst = null;
		try {
			conn = config.getConnection();

			pst = conn.prepareStatement(sql);

			config.dialect.fillStatement(pst, params);

			int result = pst.executeUpdate();

			return result > 0;
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(pst, conn);
		}
		return false;
	}


    /**
     * 生成查询结果列表
     * @param rs ResultSet
     * @param clzz Class<?>
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> List<T> generateObjectList(ResultSet rs, Class<T> clzz) throws SQLException {
    	List list = new ArrayList();

    	if (clzz.isArray()) {
    		int colAmount = rs.getMetaData().getColumnCount();
    		Class<?> elementType = clzz.getComponentType();
    		while (rs.next()) {
    			Object[] row = (Object[]) Array.newInstance(elementType, colAmount);
    			for (int i = 1; i <= colAmount;i++) {
    				row[i - 1] = rs.getObject(i);
    			}
    			list.add(row);
    		}
    	} else if(List.class.isAssignableFrom(clzz)) {
    		int colAmount = rs.getMetaData().getColumnCount();
    		while (rs.next()) {
    			List row = new ArrayList(colAmount);
    			for (int i = 1; i <= colAmount;i++) {
    				row.add(rs.getObject(i));
    			}
    			list.add(row);
    		}
    	} else {
    		while (rs.next()) {
    			list.add(rs.getObject(1));
    		}
    	}

		return list;
	}


    /**
     * 生成查询结果列表
     * @param rs ResultSet
     * @param rowMapper RowMapper<T>
     * @return
     * @throws SQLException
     */
    private <T> List<T> generateObjectList(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
    	List<T> results = new ArrayList<T>();
		int rowNum = 0;
		while (rs.next()) {
			results.add(rowMapper.mapRow(rs, rowNum++));
		}
		return results;
    }

    
    // 处理jdbc异常
	private void handleException(Connection conn, Exception e) throws JdbcExecuteException {
		e.printStackTrace();
		config.checkConnection(conn);
		throw new JdbcExecuteException(e);
	}

    
	/**
     * 获取或创建实体信息
     * @param clzz 实体类
     * @return
     */
    public ModelInfo getOrCreateModelInfo(final Class<?> clzz) {

    	ModelInfo modelInfoCached = modelInfoCache.get(clzz);
    	if (modelInfoCached != null) {
    		return modelInfoCached;
    	}

    	// 基本类型
    	if (AsmUtils.isBaseType(clzz)) {
    		throw new IllegalArgumentException("类型:" + clzz + "为基本类型,无法映射数据库.");
    	}

    	// 创建实体信息
    	final ModelInfo modelInfo = new ModelInfo();
    	modelInfo.setClzz(clzz);

    	
    	// 获取类Meta信息
    	String tableName = null;
    	if (clzz.isAnnotationPresent(javax.persistence.Table.class)) {
    		javax.persistence.Table tableAnno = clzz.getAnnotation(javax.persistence.Table.class);
    		tableName = tableAnno.name();
    	} else if(clzz.isAnnotationPresent(javax.persistence.Entity.class)) {
			javax.persistence.Entity entityAnno = clzz.getAnnotation(javax.persistence.Entity.class);
			if (!StringUtils.isEmpty(entityAnno.name())) {
				tableName = StringUtils.getLString(entityAnno.name());
			}
		}


    	// 组装TableInfo对象
		if (StringUtils.isEmpty(tableName)) {
			tableName = StringUtils.getLString(clzz.getSimpleName());
		}
    	final TableInfo tableInfo = new TableInfo(tableName, clzz);
    	modelInfo.setTableInfo(tableInfo);


    	final Map<String, Class<?>> columnTypeMap = new LinkedHashMap<String, Class<?>>();
    	@SuppressWarnings("rawtypes")
		final Map<String, AttributeInfo> attrTypeMap = new LinkedHashMap<String, AttributeInfo>();
    	// 计数器
    	final MutableInteger indexCounter = new MutableInteger(0);

    	// 遍历属性信息
    	ReflectionUtils.doWithFields(clzz, new FieldCallback() {

			@SuppressWarnings("unchecked")
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				
				// 忽略静态属性和临时属性
				if (Modifier.isTransient(field.getModifiers())
						|| Modifier.isStatic(field.getModifiers())
						|| field.isAnnotationPresent(javax.persistence.Transient.class)) {
					return;
				}

				// 是否为主键
				boolean isPrimaryKey = false;
				if (field.isAnnotationPresent(javax.persistence.Id.class)) {
					isPrimaryKey = true;
				}

				String fieldName = field.getName();
				String columnName = fieldName;
				// 定义了别名
				if (field.isAnnotationPresent(javax.persistence.Column.class)) {
					javax.persistence.Column columnAnno =
							field.getAnnotation(javax.persistence.Column.class);
					if (!StringUtils.isEmpty(columnAnno.name())) {
						columnName = columnAnno.name();
					}
				}

				// 主键
				if (isPrimaryKey) {
					tableInfo.setPrimaryKey(columnName);
				}

				// 添加字段信息
				columnTypeMap.put(columnName, field.getType());

				try {
					// 属性信息
					@SuppressWarnings("rawtypes")
					AttributeInfo attributeInfo = AttributeInfo.valueOf(
							clzz,
							field,
							columnName,
							indexCounter.incrementAndGet());
					attrTypeMap.put(fieldName, attributeInfo);
					// 主键
					if (isPrimaryKey) {
						attributeInfo.setPrimaryKey(true);
						modelInfo.setPrimaryKeyInfo(attributeInfo);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalAccessException(
							"无法创建Jdbc表字段信息:ColumnInfo,"
									+ clzz.getName() + "#" + fieldName);
				}
			}
		});

    	modelInfo.setAttrTypeMap(attrTypeMap);
    	tableInfo.setColumnTypeMap(columnTypeMap);
    	
    	// 分表策略
    	if (clzz.isAnnotationPresent(Shard.class)) {
			Shard shardAnno = clzz.getAnnotation(Shard.class);
			Class<? extends ShardStrategy> shardStrategrClass = shardAnno.value();
			try {
				tableInfo.setShardStrategy(shardStrategrClass.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalArgumentException(
						"分表策略无法初始化:" + shardStrategrClass.getName(), e);
			}
		}

    	// 初始化字段对应的sql类型
    	initAttributeSqlTypes(tableInfo, attrTypeMap);

    	ModelInfo oldModelInfo = modelInfoCache.putIfAbsent(clzz, modelInfo);

        return oldModelInfo == null ? modelInfo : oldModelInfo;
    }


    /**
     * 注册代理类
     * @param cls 实体类
     * @param proxyCls 对应的代理类
     */
    public void registerProxyClass(Class<?> cls, Class<?> proxyCls) {
    	ModelInfo modelInfo = getOrCreateModelInfo(cls);
    	modelInfo.setProxyClzz(proxyCls);
    	modelInfoCache.put(proxyCls, modelInfo);
    }


    /**
	 * 注册实体默认主键id生成器
	 * @param cls  实体类型
	 * @param idGenerator 主键id生成器接口
     */
	public void registerIdGenerator(Class<?> cls, IdGenerator<?> idGenerator) {
		this.registerIdGenerator(cls, idGenerator, 0, true);
	}


    /**
	 * 注册实体主键id生成器
	 * @param cls  实体类型
	 * @param idGenerator 主键id生成器接口
     * @param category 分区/类别
     * @param isDefault 是否为默认
     */
	public void registerIdGenerator(Class<?> cls, IdGenerator<?> idGenerator, int category, boolean isDefault) {
		ModelInfo modelInfo = getOrCreateModelInfo(cls);
		if (isDefault) {
			Map<Integer, IdGenerator<?>> idGenerators = modelInfo.getIdGenerators();
			idGenerators.put(Integer.valueOf(category), idGenerator);
		} else {
			modelInfo.setDefaultIdGenerator(idGenerator);
		}
	}

    /**
     * 关闭JDBC服务
     */
    public void close() {
    	modelInfoCache.clear();
    }


    @SuppressWarnings("rawtypes")
	private void initAttributeSqlTypes(TableInfo tableInfo, Map<String, AttributeInfo> attrTypeMap) {
    	// 初始化数据模型
    	String sql = config.dialect.forTableInfoBuilderDoBuildTableInfo(tableInfo, tableInfo.getSecondaryKey());

    	Connection conn = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
	    	conn = config.getConnection();
			pst = conn.prepareStatement(sql);

			rs = pst.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();

			buildTypes(rsmd, attrTypeMap);
		} catch (Exception e) {
			handleException(conn, e);
		} finally {
			config.close(rs, pst, conn);
		}
	}


	@SuppressWarnings("rawtypes")
	private static void buildTypes(ResultSetMetaData rsmd, Map<String, AttributeInfo> attrTypeMap) throws SQLException {
		int i = 1;
    	for (AttributeInfo attributeInfo : attrTypeMap.values()) {
			attributeInfo.setSqlType(rsmd.getColumnType(i));
			i++;
		}
	}


}
