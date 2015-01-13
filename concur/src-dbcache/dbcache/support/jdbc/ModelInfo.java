package dbcache.support.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entity信息
 * Created by Jake on 2015/1/12.
 */
public class ModelInfo {

    // 表结果信息
    private TableInfo tableInfo;

    // 属性信息
    private Map<String, ColumnInfo<?>> attrTypeMap = new LinkedHashMap<String, ColumnInfo<?>>();
    
    // 查询语句
    private String selectSql;
    
    // 插入语句
    private String insertSql;
    
    /**
     * 生成插入语句
     * @param dialect Dialect
     * @return
     */
    public String getOrCreateSelectSql(Dialect dialect) {
    	if (selectSql != null) {
    		return selectSql;
    	}
    	StringBuilder sqlBuilder = new StringBuilder();
    	dialect.forModelSave(tableInfo, sqlBuilder);
    	
    	this.selectSql = sqlBuilder.toString();
    	return this.selectSql;
    }
    

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public Map<String, ColumnInfo<?>> getAttrTypeMap() {
		return attrTypeMap;
	}

	public void setAttrTypeMap(Map<String, ColumnInfo<?>> attrTypeMap) {
		this.attrTypeMap = attrTypeMap;
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

}
