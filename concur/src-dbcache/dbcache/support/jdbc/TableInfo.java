/**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dbcache.support.jdbc;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * TableInfo save the table info like column name and column type.
 */
public class TableInfo {
	
	private String tableName;
	private String primaryKey;
	private String secondaryKey = null;
	
	@SuppressWarnings("unchecked")
	private Map<String, Class<?>> columnTypeMap = new LinkedHashMap<String, Class<?>>();	//	new HashMap<String, Class<?>>();
	
	public String getTableName() {
		return tableName;
	}
	
	public void addInfo(String columnLabel, Class<?> columnType) {
		columnTypeMap.put(columnLabel, columnType);
	}
	
	public Class<?> getColType(String columnLabel) {
		return columnTypeMap.get(columnLabel);
	}
	
	/**
	 * Model.save() need know what columns belongs to himself that he can saving to db.
	 * Think about auto saving the related table's column in the future.
	 */
	public boolean hasColumnLabel(String columnLabel) {
		return columnTypeMap.containsKey(columnLabel);
	}
	
	// 获取字段列表
	public List<String> getColumnNames() {
		return new ArrayList<String>(columnTypeMap.keySet());
	}
	
	/**
	 * update() and delete() need this method.
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}
	
	private Class<?> modelClass;
	
	public TableInfo(String tableName, Class<?> modelClass) {
		this(tableName, Dialect.getDefaultDialect().getDefaultPrimaryKey(), modelClass);
	}
	
	public TableInfo(String tableName, String primaryKey, Class<?> modelClass) {
		if (StringUtils.isBlank(tableName))
			throw new IllegalArgumentException("Table name can not be blank.");
		if (StringUtils.isBlank(primaryKey))
			throw new IllegalArgumentException("Primary key can not be blank.");
		if (modelClass == null)
			throw new IllegalArgumentException("Model class can not be null.");
		
		this.tableName = tableName.trim();
		setPrimaryKey(primaryKey.trim());	// this.primaryKey = primaryKey.trim();
		this.modelClass = modelClass;
	}
	
	public Map<String, Class<?>> getColumnTypeMap() {
		return columnTypeMap;
	}

	private void setPrimaryKey(String primaryKey) {
		String[] keyArr = primaryKey.split(",");
		if (keyArr.length > 1) {
			if (StringUtils.isBlank(keyArr[0]) || StringUtils.isBlank(keyArr[1]))
				throw new IllegalArgumentException("The composite primary key can not be blank.");
			this.primaryKey = keyArr[0].trim();
			this.secondaryKey = keyArr[1].trim();
		}
		else {
			this.primaryKey = primaryKey;
		}
	}
	
	public String getSecondaryKey() {
		return secondaryKey;
	}
	
	public void setColumnTypeMap(Map<String, Class<?>> columnTypeMap) {
		this.columnTypeMap = columnTypeMap;
	}

	public Class<?> getModelClass() {
		return modelClass;
	}
}



