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

package dbcache.support.jdbc.dialect;

import dbcache.support.jdbc.Dialect;
import dbcache.support.jdbc.TableInfo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * PostgreSqlDialect.
 */
public class PostgreSqlDialect extends Dialect {
	
	public String forTableInfoBuilderDoBuildTableInfo(String tableName) {
		return "select * from \"" + tableName + "\" where 1 = 2";
	}
	
	public void forSave(TableInfo tableInfo, Map<String, Object> attrs, StringBuilder sql) {
		sql.append("insert into \"").append(tableInfo.getTableName()).append("\"(");
		StringBuilder temp = new StringBuilder(") values(");
		boolean first = true;
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (tableInfo.hasColumnLabel(colName)) {
				if (!first) {
					sql.append(", ");
				} else {
					first = false;
				}
				sql.append("\"").append(colName).append("\"");
				temp.append("?");
			}
		}
		sql.append(temp.toString()).append(")");
	}
	
	public String forDeleteById(TableInfo tInfo) {
		String primaryKey = tInfo.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from \"");
		sql.append(tInfo.getTableName());
		sql.append("\" where \"").append(primaryKey).append("\" = ?");
		return sql.toString();
	}
	
	public void forUpdate(TableInfo tableInfo, Map<String, Object> attrs, Set<String> modifyFlag, String primaryKey, StringBuilder sql) {
		sql.append("update \"").append(tableInfo.getTableName()).append("\" set ");
		boolean first = true;
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (!primaryKey.equalsIgnoreCase(colName) && modifyFlag.contains(colName) && tableInfo.hasColumnLabel(colName)) {
				if (!first) {
					sql.append(", ");
				} else {
					first = false;
				}
				sql.append("\"").append(colName).append("\" = ? ");
			}
		}
		sql.append(" where \"").append(primaryKey).append("\" = ?");
	}
	
	public String forFindById(TableInfo tInfo, String columns) {
		StringBuilder sql = new StringBuilder("select ");
		if (columns.trim().equals("*")) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(",");
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append("\"").append(columnsArray[i].trim()).append("\"");
			}
		}
		sql.append(" from \"");
		sql.append(tInfo.getTableName());
		sql.append("\" where \"").append(tInfo.getPrimaryKey()).append("\" = ?");
		return sql.toString();
	}
	
	
	@Override
	public String forFindByColumn(TableInfo tInfo, String columns,
			String columnName) {
		StringBuilder sql = new StringBuilder("select ");
		if (columns.trim().equals("*")) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(",");
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append("\"").append(columnsArray[i].trim()).append("\"");
			}
		}
		sql.append(" from \"");
		sql.append(tInfo.getTableName());
		sql.append("\" where \"").append(columnName).append("\" = ?");
		return sql.toString();
	}


	public void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		int offset = pageSize * (pageNumber - 1);
		sql.append(select).append(" ");
		sql.append(sqlExceptSelect);
		sql.append(" limit ").append(pageSize).append(" offset ").append(offset);
	}

}
