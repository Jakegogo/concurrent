package dbcache.support.jdbc;

import dbcache.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jdbc Dao支持
 * <br/> 支持javax.persistence.*注解
 * Created by Jake on 2015/1/10.
 */
@Component
public class JdbcSupport {

	@Autowired
    private Config config;

    /** 实体信息缓存 */
    private ConcurrentMap<Class<?>, ModelInfo> modelInfoCache = new ConcurrentHashMap<Class<?>, ModelInfo>();


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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		} finally {
			config.close(pst, conn);
		}
    	return false;
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
			e.printStackTrace();
		} finally {
			config.close(pst, conn);
		}
    	return false;
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
			e.printStackTrace();
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
    @SuppressWarnings("unchecked")
	public <T> List<T> listIdByAttr(final Class<T> clzz, String attrName, Object attrValue) {
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
			return (List<T>) modelInfo.generateIdList(rs);
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		} finally {
			config.close(rs, pst, conn);
		}
    	
    	return null;
    }
    
    
    /**
     * 获取或创建实体信息
     * @param clzz 实体类
     * @return
     */
    public ModelInfo getOrCreateModelInfo(final Class<?> clzz) {
    	// 基本类型
    	if(clzz.isPrimitive()) {
    		
    	}
    	
    	ModelInfo modelInfoCached = modelInfoCache.get(clzz);
    	if(modelInfoCached != null) {
    		return modelInfoCached;
    	}
    	
    	// 创建实体信息
    	final ModelInfo modelInfo = new ModelInfo();
    	modelInfo.setClzz(clzz);
    	
    	String tableName = null;
    	// 获取类Meta信息
    	if (clzz.isAnnotationPresent(javax.persistence.Table.class)) {
    		javax.persistence.Table tableAnno = clzz.getAnnotation(javax.persistence.Table.class);
    		tableName = tableAnno.name();
    	} else {
    		tableName = StringUtils.getLString(clzz.getSimpleName());
    	}
    	// 创建TableInfo对象
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
				if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) ||
						field.isAnnotationPresent(javax.persistence.Transient.class)) {
					return;
				}
				
				// 是否为主键
				boolean isPrimaryKey = false;
				if(field.isAnnotationPresent(javax.persistence.Id.class)) {
					isPrimaryKey = true;
				}
				
				String fieldName = field.getName();
				String columnName = fieldName;
				// 定义了别名
				if(field.isAnnotationPresent(javax.persistence.Column.class)) {
					javax.persistence.Column columnAnno = field.getAnnotation(javax.persistence.Column.class);
					columnName = columnAnno.name();
				}
				
				// 主键
				if(isPrimaryKey) {
					tableInfo.setPrimaryKey(columnName);
				}
				
				columnTypeMap.put(columnName, field.getType());
				
				try {
					// 属性信息
					@SuppressWarnings("rawtypes")
					AttributeInfo attributeInfo = AttributeInfo.valueOf(clzz, field, columnName, indexCounter.incrementAndGet());
					attrTypeMap.put(fieldName, attributeInfo);
					if(isPrimaryKey) {
						modelInfo.setPrimaryKeyInfo(attributeInfo);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalAccessException("无法创建Jdbc表字段信息:ColumnInfo");
				}
			}
		});
    	
    	modelInfo.setAttrTypeMap(attrTypeMap);
    	tableInfo.setColumnTypeMap(columnTypeMap);
    	
    	// 初始化字段对应的sql类型
    	initAttributeSqlTypes(tableInfo, attrTypeMap);
    	
    	ModelInfo oldModelInfo = modelInfoCache.putIfAbsent(clzz, modelInfo);
    	
        return oldModelInfo == null ? modelInfo : oldModelInfo;
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
			e.printStackTrace();
		} finally {
			config.close(rs, pst, conn);
		}
	}


	@SuppressWarnings("rawtypes")
	private static final void buildTypes(ResultSetMetaData rsmd, Map<String, AttributeInfo> attrTypeMap) throws SQLException {
		int i = 1;
    	for (AttributeInfo attributeInfo : attrTypeMap.values()) {
			attributeInfo.setSqlType(rsmd.getColumnType(i));
			i++;
		}
	}

	// 可变Integer  
    public static final class MutableInteger{  
        private int val;  
        public MutableInteger(int val){  
            this.val = val;  
        }  
        public int get(){  
            return this.val;  
        }  
        public void set(int val){  
            this.val = val;  
        }
        public int incrementAndGet() {
        	return ++ this.val;
        }
        // 为了方便打印  
        public String toString() {  
            return Integer.toString(val);  
        }  
    }
    
    public static void main(String[] args) {
		String sql = new JdbcSupport().getOrCreateModelInfo(dbcache.test.Entity.class).getOrCreateFindIdByAttributeSql(Dialect.getDefaultDialect(), "num");
		System.out.println(sql);
	}
    

}
