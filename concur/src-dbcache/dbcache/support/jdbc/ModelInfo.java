package dbcache.support.jdbc;

import dbcache.pkey.IdGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Entity信息
 * Created by Jake on 2015/1/12.
 */
public class ModelInfo {

	// 实体类
	private Class<?> clzz;

	// 代理类
	private Class<?> proxyClzz;

    // 表结果信息
    private TableInfo tableInfo;

    // 主键信息
    private AttributeInfo<Object> primaryKeyInfo;

    // 属性信息
    @SuppressWarnings("rawtypes")
	private Map<String, AttributeInfo> attrTypeMap = new LinkedHashMap<String, AttributeInfo>();

	// 属性列表
	@SuppressWarnings("rawtypes")
	private ArrayList<AttributeInfo> columnInfos;

	/**
	 * 实体主键ID生成map {类别ID : {实体类： 主键id生成器} }
	 * <br/>category - IdGenerator
	 */
	private Map<Integer, IdGenerator<?>> idGenerators = new IdentityHashMap<Integer, IdGenerator<?>> ();

	/**  默认主键id生成器  */
	private IdGenerator<?> defaultIdGenerator;

    // 查询语句
    private String selectSql;

    // 插入语句
    private String insertSql;

    // 删除语句
    private String deleteSql;

    // 更新语句
    private String updateSql;

    // 查询最大Id语句
    private String selectMaxIdSql;

	// 按字段查询Id语句
    private Map<String, String> findIdByColumnSqlMap = new HashMap<String, String>();

    // 按字段查询语句
    private Map<String, String> findByColumnSqlMap = new HashMap<String, String>();
    
    // 按字段更新语句
    private Map<Integer, String> updateByColumnSqlMap = new HashMap<Integer, String>();

	// 按字段更新语句
    private Map<String, String> updateByFieldSqlMap = new HashMap<String, String>();
    
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
     * 生成更新语句
     * @param dialect Dialect
     * @param modifiedFields 修改过的属性集合
     * @return
     */
    public String getOrCreateUpdateSql(Dialect dialect, List<String> modifiedFields) {
    	
    	if (modifiedFields.size() == 0) {
    		return this.getOrCreateUpdateSql(dialect);
    	}
    	
    	String sql = null, key = null;
    	if (modifiedFields.size() == 1) {
    		key = modifiedFields.get(0);
    		
    		sql = this.updateByFieldSqlMap.get(key);
    		if (sql != null) {
    			return sql;
    		}
    	}
    	
    	List<String> modifiedColumns = new ArrayList<String>();
		for (String fieldName : modifiedFields) {
			AttributeInfo attributeInfo = this.attrTypeMap.get(fieldName);
			if (attributeInfo == null) {
				throw new IllegalArgumentException("不存在的属性:" + fieldName + " [" + this.clzz + "]");
			}
			if (!attributeInfo.isPrimaryKey()) {
				modifiedColumns.add(attributeInfo.getColumnName());
			}
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
    	dialect.forDbUpdate(tableInfo, modifiedColumns, sqlBuilder);

    	sql = sqlBuilder.toString();
    	if (modifiedFields.size() == 1) {
    		this.updateByFieldSqlMap.put(key, sql);
    	}
    	
    	return sql;
    }

    
    /**
     * 生成更新语句
     * @param dialect Dialect
     * @param modifiedFields 修改过的属性集合
     * @return
     */
    public String getOrCreateUpdateSql(List<Integer> modifiedFields, Dialect dialect) {
    	
    	if (modifiedFields.size() == 0) {
    		return this.getOrCreateUpdateSql(dialect);
    	}
    	
    	String sql = null; int key = 1;
    	if (modifiedFields.size() <= 3) {
    		for (Integer fieldIndex : modifiedFields) {
    			key = key * 31 + fieldIndex;
        	}
    		sql = this.updateByColumnSqlMap.get(key);
    		if (sql != null) {
    			return sql;
    		}
    	}
    	
    	List<String> modifiedColumns = new ArrayList<String>(modifiedFields.size());
    	for (Integer fieldIndex : modifiedFields) {
			AttributeInfo attributeInfo = this.columnInfos.get(fieldIndex);
			modifiedColumns.add(attributeInfo.getColumnName());
    	}
    	
		StringBuilder sqlBuilder = new StringBuilder();
    	dialect.forDbUpdate(tableInfo, modifiedColumns, sqlBuilder);

    	sql = sqlBuilder.toString();
    	if (modifiedFields.size() <= 3) {
    		this.updateByColumnSqlMap.put(key, sql);
    	}
    	
    	return sql;
    }
    

    /**
     * 生成查询最大主键语句
     * @param dialect Dialect
     * @return
     */
    public String getOrCreateSelectMaxIdSql(Dialect dialect) {
		if (selectMaxIdSql != null) {
			return selectMaxIdSql;
		}
    	this.selectMaxIdSql = dialect.forModelSelectMax(tableInfo, tableInfo.getPrimaryKey());
    	return this.selectMaxIdSql;
	}


    /**
     * 生成按属性查询Id语句
     * @param dialect Dialect
     * @param attribute 字段名
     * @return
     */
    public String getOrCreateFindIdByAttributeSql(Dialect dialect, String attribute) {
    	String sql = findIdByColumnSqlMap.get(attribute);
    	if (sql != null) {
    		return sql;
    	}

    	AttributeInfo<?> attributeInfo = this.attrTypeMap.get(attribute);
    	if (attributeInfo == null) {
    		throw new IllegalArgumentException("attribute [" + attribute + "] not found in " + this.clzz.getName());
    	}

    	String column = attributeInfo.getColumnName();
    	sql = dialect.forModelFindIdByColumn(tableInfo, column);
    	this.findIdByColumnSqlMap.put(attribute, sql);
    	return sql;
    }


    /**
     * 生成按属性查询语句
     * @param dialect Dialect
     * @param attribute 字段名
     * @return
     */
    public String getOrCreateFindByAttributeSql(Dialect dialect, String attribute) {
    	String sql = findByColumnSqlMap.get(attribute);
    	if (sql != null) {
    		return sql;
    	}

    	AttributeInfo<?> attributeInfo = this.attrTypeMap.get(attribute);
    	if (attributeInfo == null) {
    		throw new IllegalArgumentException("attribute [" + attribute + "] not found in " + this.clzz.getName());
    	}

    	String column = attributeInfo.getColumnName();
    	sql = dialect.forModelFindByColumn(tableInfo, column);
    	this.findByColumnSqlMap.put(attribute, sql);
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
		if (rs.next()) {
			Class<?> clzz = this.proxyClzz != null ? this.proxyClzz : this.clzz;
			Object instance = clzz.newInstance();

			int columnIndex = 1;
			for (AttributeInfo<Object> columnInfo : this.columnInfos) {
				columnInfo.setFromPersistValue(instance, this.getRsVal(rs, columnIndex, columnInfo.getSqlType(), columnInfo));
				columnIndex++;
			}
			return instance;
		}

		return null;
	}


    /**
     * 根据结果集生成实体列表
     * @param rs 查询结果集
     * @return 实体对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List generateEntityList(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException  {
    	List list = new ArrayList();

    	while (rs.next()) {
			Class<?> clzz = this.proxyClzz != null ? this.proxyClzz : this.clzz;
			Object instance = clzz.newInstance();

			int columnIndex = 1;
			for (AttributeInfo<Object> columnInfo : this.columnInfos) {
				columnInfo.setFromPersistValue(instance, this.getRsVal(rs, columnIndex, columnInfo.getSqlType(), columnInfo));
				columnIndex++;
			}
			list.add(instance);
		}

		return list;
	}

    /**
     * 根据结果集生成实体Id列表
     * @param rs 查询结果集
     * @return 实体对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List generateIdList(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
    	List list = new ArrayList();

    	while (rs.next()) {
			list.add(rs.getObject(1));
		}

		return list;
	}

    /**
     * 生成唯一结果
     * @param rs 查询结果集
     * @return
     */
    public Object generateUniqueResult(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
    	if (rs.next()) {
    		return rs.getObject(1);
    	}
		return null;
	}

    /**
     * 获取保存的sql参数
     * @param entity 实体
     * @return
     */
    @SuppressWarnings("unchecked")
	public Object[] getSaveParams(Object entity) {
		Object[] sqlParams = new Object[this.columnInfos.size()];
		int i = 0;
		for (AttributeInfo<Object> columnInfo : this.columnInfos) {
			sqlParams[i] = columnInfo.getPersistValue(entity);
			i++;
		}
		return sqlParams;
	}

    /**
     * 获取保存的sql参数
     * @param entity 实体
     * @return
     */
    @SuppressWarnings("unchecked")
	public Object[] getAutoIdSaveParams(Object entity) {
    	Object[] sqlParams = new Object[this.columnInfos.size()];
		int i = 0;Object columnVal;
		for (AttributeInfo<Object> columnInfo : this.columnInfos) {
			columnVal = columnInfo.getPersistValue(entity);
			if (columnInfo.isPrimaryKey() && columnVal == null) {
				if (this.defaultIdGenerator != null) {
					sqlParams[i] = this.defaultIdGenerator.generateId();
				}
			} else {
				sqlParams[i] = columnVal;
			}
			i++;
		}
		return sqlParams;
	}


    /**
     * 获取保存的sql参数
     * @param entity 实体
     * @param category 分段类别
     * @return
     */
    @SuppressWarnings("unchecked")
	public Object[] getAutoIdSaveParams(Object entity, int category) {
    	Object[] sqlParams = new Object[this.columnInfos.size()];
		int i = 0;Object columnVal;
		for (AttributeInfo<Object> columnInfo : this.columnInfos) {
			columnVal = columnInfo.getPersistValue(entity);
			if (columnInfo.isPrimaryKey() && columnVal == null) {
				// 获取Id生成器
				IdGenerator<?> idGenerator = this.idGenerators.get(Integer.valueOf(category));
				if (idGenerator != null) {
					sqlParams[i] = idGenerator.generateId();
				}
			} else {
				sqlParams[i] = columnVal;
			}
			i++;
		}
		return sqlParams;
	}


    /**
     * 获取更新的sql参数
     * @param entity 实体
     * @return
     */
    @SuppressWarnings("unchecked")
	public Object[] getUpdateParams(Object entity) {
    	Object[] sqlParams = new Object[this.columnInfos.size()];
		int i = 0;
		for (AttributeInfo<Object> columnInfo : this.columnInfos) {
			if (!columnInfo.isPrimaryKey()) {
				sqlParams[i] = columnInfo.getPersistValue(entity);
				i++;
			}
		}
		sqlParams[this.columnInfos.size() - 1] = primaryKeyInfo.getPersistValue(entity);
		return sqlParams;
	}

    
   /**
    * 获取更新的sql参数
    * @param entity 实体
    * @param modifiedFields 修改过的属性集合
    * @return
    */
   @SuppressWarnings("unchecked")
	public Object[] getUpdateParams(Object entity, List<String> modifiedFields) {
		if (modifiedFields.size() == 0) {
			return this.getUpdateParams(entity);
		}
   		Object[] sqlParams = new Object[this.columnInfos.size()];
		int i = 0;
		for (String fieldName : modifiedFields) {
			AttributeInfo<Object> columnInfo = this.attrTypeMap.get(fieldName);
			if (columnInfo != null && !columnInfo.isPrimaryKey()) {
				sqlParams[i] = columnInfo.getPersistValue(entity);
				i++;
			}
		}
		sqlParams[this.columnInfos.size() - 1] = primaryKeyInfo.getPersistValue(entity);
		return sqlParams;
	}
    

   /**
    * 获取更新的sql参数
    * @param entity 实体
    * @param modifiedFields 修改过的属性数组(线程安全)
    * @return
    */
	@SuppressWarnings("unchecked")
	public Object[] getUpdateParams(List<Integer> modifiedFields, Object entity) {
		if (modifiedFields.size() == 0) {
			return this.getUpdateParams(entity);
		}

	   	Object[] sqlParams = new Object[modifiedFields.size() + 1];
		
	   	int i = 0;
	   	for (Integer fieldIndex : modifiedFields) {
			AttributeInfo attributeInfo = this.columnInfos.get(fieldIndex);
			if (attributeInfo != null && !attributeInfo.isPrimaryKey()) {
				sqlParams[i++] = attributeInfo.getPersistValue(entity);
			}
		}
	   	
		sqlParams[modifiedFields.size()] = primaryKeyInfo.getPersistValue(entity);
		return sqlParams;
	}
   
   
   
    @SuppressWarnings("rawtypes")
	private Object getRsVal(ResultSet rs, int i, int type, AttributeInfo columnInfo) throws SQLException {
    	Object value = null;
    	if (columnInfo.getType() == java.util.Date.class) {
    		java.sql.Date typeDate = rs.getDate(i);
    		if (typeDate != null) {
    			value = new Date(typeDate.getTime());
    		}
		} else if (type < Types.BLOB)
			value = rs.getObject(i);
		else if (type == Types.CLOB)
			value = handleClob(rs.getClob(i));
		else if (type == Types.NCLOB)
			value = handleClob(rs.getNClob(i));
		else if (type == Types.BLOB)
			value = handleBlob(rs.getBlob(i));
		else
			value = rs.getObject(i);
    	return value;
    }

    
    public static byte[] handleBlob(Blob blob) throws SQLException {
		if (blob == null)
			return null;

		InputStream is = null;
		try {
			is = blob.getBinaryStream();
			byte[] data = new byte[(int) blob.length()];		// byte[] data = new byte[is.available()];
			is.read(data);
			is.close();
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {if (is != null) is.close();} catch (IOException e) {throw new RuntimeException(e);}
		}
	}

    
	public static String handleClob(Clob clob) throws SQLException {
		if (clob == null)
			return null;

		Reader reader = null;
		try {
			reader = clob.getCharacterStream();
			char[] buffer = new char[(int)clob.length()];
			reader.read(buffer);
			return new String(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {if (reader != null) reader.close();} catch (IOException e) {throw new RuntimeException(e);}
		}
	}


    /**
     * 获取删除的sql参数
     * @param entity 实体
     * @return
     */
	public Object getDeleteParam(Object entity) {
		return primaryKeyInfo.getPersistValue(entity);
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public Class<?> getClzz() {
		return clzz;
	}

	/**
	 * 设置实体类
	 *
	 * @param clzz
	 */
	public void setClzz(Class<?> clzz) {
		this.clzz = clzz;
	}

	public Class<?> getProxyClzz() {
		return proxyClzz;
	}

	public void setProxyClzz(Class<?> proxyClzz) {
		this.proxyClzz = proxyClzz;
	}

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public AttributeInfo<Object> getPrimaryKeyInfo() {
		return primaryKeyInfo;
	}

	public void setPrimaryKeyInfo(AttributeInfo<Object> primaryKeyInfo) {
		this.primaryKeyInfo = primaryKeyInfo;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, AttributeInfo> getAttrTypeMap() {
		return attrTypeMap;
	}

	@SuppressWarnings("rawtypes")
	public void setAttrTypeMap(Map<String, AttributeInfo> attrTypeMap) {
		this.attrTypeMap = attrTypeMap;
		this.findByColumnSqlMap = new HashMap<String, String>(
				attrTypeMap.size());
		this.columnInfos = new ArrayList<AttributeInfo>(attrTypeMap.values());
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

	public String getSelectMaxIdSql() {
		return selectMaxIdSql;
	}

	public void setSelectMaxIdSql(String selectMaxIdSql) {
		this.selectMaxIdSql = selectMaxIdSql;
	}

	public Map<String, String> getFindByColumnSqlMap() {
		return findByColumnSqlMap;
	}

	public void setFindByColumnSqlMap(Map<String, String> findByColumnSqlMap) {
		this.findByColumnSqlMap = findByColumnSqlMap;
	}

	public Map<Integer, IdGenerator<?>> getIdGenerators() {
		return idGenerators;
	}

	public void setIdGenerators(Map<Integer, IdGenerator<?>> idGenerators) {
		this.idGenerators = idGenerators;
	}

	public IdGenerator<?> getDefaultIdGenerator() {
		return defaultIdGenerator;
	}

	public void setDefaultIdGenerator(IdGenerator<?> defaultIdGenerator) {
		this.defaultIdGenerator = defaultIdGenerator;
	}


}
