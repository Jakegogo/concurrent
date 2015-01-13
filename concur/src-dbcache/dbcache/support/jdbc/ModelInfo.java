package dbcache.support.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entity信息
 * Created by Jake on 2015/1/12.
 */
public class ModelInfo {

	// 实体类
	private Class<?> clzz;
	
    // 表结果信息
    private TableInfo tableInfo;

    // 属性信息
    @SuppressWarnings("rawtypes")
	private Map<String, ColumnInfo> attrTypeMap = new LinkedHashMap<String, ColumnInfo>();
    
    // 查询语句
    private String selectSql;
    
    // 插入语句
    private String insertSql;
    
    // 删除语句
    private String deleteSql;
    
    // 更新语句
    private String updateSql;
    
    // 按字段查询语句
    private Map<String, String> findByColumnSqlMap = new HashMap<String, String>();
    
    /**
     * 生成插入语句
     * @param dialect Dialect
     * @return
     */
    public String getOrCreateSelectSql(Dialect dialect) {
    	if (selectSql != null) {
    		return selectSql;
    	}
    	    	
    	this.selectSql = dialect.forModelFindById(tableInfo);
    	return this.selectSql;
    }
    
    
    /**
     * 生成插入语句
     * @param dialect Dialect
     * @return
     */
    public String getOrCreateSaveSql(Dialect dialect) {
    	if (insertSql != null) {
    		return insertSql;
    	}
    	StringBuilder sqlBuilder = new StringBuilder();
    	dialect.forModelSave(tableInfo, sqlBuilder);
    	
    	this.insertSql = sqlBuilder.toString();
    	return this.insertSql;
    }
    
    
    /**
     * 生成删除语句
     * @param dialect Dialect
     * @return
     */
    public String getOrCreateDeleteSql(Dialect dialect) {
    	if (deleteSql != null) {
    		return deleteSql;
    	}
    	    	
    	this.deleteSql = dialect.forModelDeleteById(tableInfo);
    	return this.deleteSql;
    }
    
    
    /**
     * 生成更新语句
     * @param dialect Dialect
     * @return
     */
    public String getOrCreateUpdateSql(Dialect dialect) {
    	if (updateSql != null) {
    		return updateSql;
    	}
    	StringBuilder sqlBuilder = new StringBuilder();
    	dialect.forModelUpdate(tableInfo, sqlBuilder);
    	
    	this.updateSql = sqlBuilder.toString();
    	return this.updateSql;
    }
    
    
    /**
     * 生成按字段查询语句
     * @param dialect Dialect
     * @param column 字段名
     * @return
     */
    public String getOrCreateFindByColumnSql(Dialect dialect, String column) {
    	String sql = findByColumnSqlMap.get(column);
    	if (sql != null) {
    		return sql;
    	}
    	
    	sql = dialect.forModelFindByColumn(tableInfo, column);
    	this.findByColumnSqlMap.put(column, sql);
    	return sql;
    }
    
    
    /**
     * 根据结果集生成实体
     * @param rs 查询结果集
     * @return 实体对象
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     */
    @SuppressWarnings("unchecked")
	public Object generateEntity(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
    	Object instance = clzz.newInstance();
    	
    	int columnIndex = 1;
    	for(ColumnInfo<Object> columnInfo : this.attrTypeMap.values()) {
    		columnInfo.setValue(instance, rs.getObject(columnIndex));
    		columnIndex ++;
    	}
    	
		return instance;
	}
    

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public Class<?> getClzz() {
		return clzz;
	}


	public void setClzz(Class<?> clzz) {
		this.clzz = clzz;
	}


	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, ColumnInfo> getAttrTypeMap() {
		return attrTypeMap;
	}

	@SuppressWarnings("rawtypes")
	public void setAttrTypeMap(Map<String, ColumnInfo> attrTypeMap) {
		this.attrTypeMap = attrTypeMap;
		this.findByColumnSqlMap = new HashMap<String, String>(attrTypeMap.size());
	}

	public String getSelectSql() {
		return selectSql;
	}

	public void setSelectSql(String selectSql) {
		this.selectSql = selectSql;
	}

	public String getInsertSql() {
		return insertSql;
	}

	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}


	public String getDeleteSql() {
		return deleteSql;
	}


	public void setDeleteSql(String deleteSql) {
		this.deleteSql = deleteSql;
	}


	public String getUpdateSql() {
		return updateSql;
	}


	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}


	public Map<String, String> getFindByColumnSqlMap() {
		return findByColumnSqlMap;
	}


	public void setFindByColumnSqlMap(Map<String, String> findByColumnSqlMap) {
		this.findByColumnSqlMap = findByColumnSqlMap;
	}


}
