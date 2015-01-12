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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * OracleDialect.
 */
public class OracleDialect extends Dialect {
	
	public String forTableInfoBuilderDoBuildTableInfo(String tableName) {
		return "select * from " + tableName + " where rownum = 0";
	}
	
	// insert into table (id,name) values(seq.nextval, ？)
	public void forSave(TableInfo tableInfo, Map<String, Object> attrs, StringBuilder sql) {
		sql.append("insert into ").append(tableInfo.getTableName()).append("(");
		StringBuilder temp = new StringBuilder(") values(");
		String pKey = tableInfo.getPrimaryKey();
		int count = 0;
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (tableInfo.hasColumnLabel(colName)) {
				if (count++ > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append(colName);
				Object value = e.getValue();
				if(value instanceof String && colName.equalsIgnoreCase(pKey) && ((String)value).endsWith(".nextval")) {
				    temp.append(value);
				}else{
				    temp.append("?");
				}
			}
		}
		sql.append(temp.toString()).append(")");
	}
	
	public String forDeleteById(TableInfo tInfo) {
		String pKey = tInfo.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(pKey).append(" = ?");
		return sql.toString();
	}
	
	public void forUpdate(TableInfo tableInfo, Map<String, Object> attrs, Set<String> modifyFlag, String pKey, StringBuilder sql) {
		sql.append("update ").append(tableInfo.getTableName()).append(" set ");
		boolean first = true;
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (!pKey.equalsIgnoreCase(colName) && modifyFlag.contains(colName) && tableInfo.hasColumnLabel(colName)) {
				if (!first) {
					sql.append(", ");
				} else {
					first = false;
				}
				sql.append(colName).append(" = ? ");
			}
		}
		sql.append(" where ").append(pKey).append(" = ?");
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
				sql.append(columnsArray[i].trim());
			}
		}
		sql.append(" from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(tInfo.getPrimaryKey()).append(" = ?");
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
				sql.append(columnsArray[i].trim());
			}
		}
		sql.append(" from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(columnName).append(" = ?");
		return sql.toString();
	}

	public void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		int satrt = (pageNumber - 1) * pageSize + 1;
		int end = pageNumber * pageSize;
		sql.append("select * from ( select row_.*, rownum rownum_ from (  ");
		sql.append(select).append(" ").append(sqlExceptSelect);
		sql.append(" ) row_ where rownum <= ").append(end).append(") table_alias");
		sql.append(" where table_alias.rownum_ >= ").append(satrt);
	}
	
	public boolean isOracle() {
		return true;
	}
	
	public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		for (int i=0, size=paras.size(); i<size; i++) {
			Object value = paras.get(i);
			if (value instanceof java.sql.Date)
				pst.setDate(i + 1, (java.sql.Date)value);
			else
				pst.setObject(i + 1, value);
		}
	}
	
	public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		for (int i=0; i<paras.length; i++) {
			Object value = paras[i];
			if (value instanceof java.sql.Date)
				pst.setDate(i + 1, (java.sql.Date)value);
			else
				pst.setObject(i + 1, value);
		}
	}
	
	public String getDefaultPrimaryKey() {
		return "ID";
	}

}
