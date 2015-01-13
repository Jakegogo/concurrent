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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jdbc Dao支持
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
     * 获取或创建实体信息
     * @param clzz 实体类
     * @return
     */
    public ModelInfo getOrCreateModelInfo(final Class<?> clzz) {
    	
    	ModelInfo modelInfo = modelInfoCache.get(clzz);
    	if(modelInfo != null) {
    		return modelInfo;
    	}
    	
    	// 创建实体信息
    	modelInfo = new ModelInfo();
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
		final Map<String, ColumnInfo> attrTypeMap = new LinkedHashMap<String, ColumnInfo>();
    	// 遍历属性信息
    	ReflectionUtils.doWithFields(clzz, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				// 忽略静态属性和临时属性
				if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) ||
						field.isAnnotationPresent(javax.persistence.Transient.class)) {
					return;
				}
				
				// 主键
				if(field.isAnnotationPresent(javax.persistence.Id.class)) {
					tableInfo.setPrimaryKey(field.getName());
				}
				
				columnTypeMap.put(field.getName(), field.getType());
				try {
					attrTypeMap.put(field.getName(), ColumnInfo.valueOf(clzz, field));
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalAccessException("无法创建Jdbc表字段信息:ColumnInfo");
				}
			}
		});
    	
    	modelInfo.setAttrTypeMap(attrTypeMap);
    	tableInfo.setColumnTypeMap(columnTypeMap);
    	ModelInfo oldModelInfo = modelInfoCache.putIfAbsent(clzz, modelInfo);
    	
        return oldModelInfo == null ? modelInfo : oldModelInfo;
    }

    
    
    public static void main(String[] args) {
		String sql = new JdbcSupport().getOrCreateModelInfo(dbcache.test.Entity.class).getOrCreateFindByColumnSql(Dialect.getDefaultDialect(), "num");
		System.out.println(sql);
	}
    

}
