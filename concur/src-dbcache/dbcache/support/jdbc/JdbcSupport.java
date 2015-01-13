package dbcache.support.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.Transient;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.sun.xml.internal.stream.Entity;

import dbcache.utils.StringUtils;

/**
 * Jdbc Dao支持
 * Created by Jake on 2015/1/10.
 */
@Component
public class JdbcSupport {


    @Autowired
    private DataSource dataSource;

    /** 实体信息缓存 */
    private ConcurrentMap<Class<?>, ModelInfo> modelInfoCache = new ConcurrentHashMap<Class<?>, ModelInfo>();


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
    	final Map<String, ColumnInfo<?>> attrTypeMap = new LinkedHashMap<String, ColumnInfo<?>>();
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
		String sql = new JdbcSupport().getOrCreateModelInfo(dbcache.test.Entity.class).getOrCreateSelectSql(Dialect.getDefaultDialect());
		System.out.println(sql);
	}
    

}
