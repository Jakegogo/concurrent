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
import java.util.Collection;
import java.util.List;


/**
 * OracleDialect.
 */
public class OracleDialect extends Dialect {
	
	public String forTableInfoBuilderDoBuildTableInfo(TableInfo tInfo, String tableName) {
		StringBuilder sql = new StringBuilder("select ");
		boolean first = true;
		for (String column : tInfo.getColumnTypeMap().keySet()) {
			if (!first) {
				sql.append(", ");
			} else {
				first = false;
			}
			sql.append(column.trim());
		}
		sql.append(" from ");
		sql.append(tInfo.getTableName());
		sql.append(" where rownum = 0");
		return sql.toString();
	}
	
	// insert into table (id,name) values(seq.nextval, ？)
	public void forModelSave(TableInfo tableInfo, StringBuilder sql) {
		sql.append("insert into ").append(tableInfo.getTableName()).append("(");
		StringBuilder temp = new StringBuilder(") values(");
		int count = 0;
		for (String colName : tableInfo.getColumnNames()) {
			if (count++ > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append(colName);
			temp.append("?");
		}
		sql.append(temp.toString()).append(")");
	}
	
	public String forModelDeleteById(TableInfo tInfo) {
		String pKey = tInfo.getPrimaryKey();
		String sql = "delete from " +
				tInfo.getTableName() +
				" where " + pKey + " = ?";
		return sql;
	}
	
	public void forModelUpdate(TableInfo tableInfo, StringBuilder sql) {
		sql.append("update ").append(tableInfo.getTableName()).append(" set ");
		boolean first = true;
		for (String colName : tableInfo.getColumnNames()) {
			if (!tableInfo.getPrimaryKey().equalsIgnoreCase(colName)) {
				if (!first) {
					sql.append(", ");
				} else {
					first = false;
				}
				sql.append(colName).append(" = ? ");
			}
		}
		sql.append(" where ").append(tableInfo.getPrimaryKey()).append(" = ?");
	}
	
	@Override
	public void forDbUpdate(TableInfo tableInfo, Collection<String> modifyColumns,
			StringBuilder sql) {
		sql.append("update ").append(tableInfo.getTableName()).append(" set ");
		boolean first = true;
		for (String colName : modifyColumns) {
			if (tableInfo.hasColumnLabel(colName)) {
				if (!first) {
					sql.append(", ");
				} else {
					first = false;
				}
				sql.append(colName).append(" = ? ");
			}
		}
		sql.append(" where ").append(tableInfo.getPrimaryKey()).append(" = ?");
	}
	
	public String forModelFindById(TableInfo tInfo) {
		StringBuilder sql = new StringBuilder("select ");
		boolean first = true;
		for (String column : tInfo.getColumnTypeMap().keySet()) {
			if (!first) {
				sql.append(", ");
			} else {
				first = false;
			}
			sql.append(column.trim());
		}
		sql.append(" from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(tInfo.getPrimaryKey()).append(" = ?");
		return sql.toString();
	}
	
	@Override
	public String forModelFindByColumn(TableInfo tInfo, String columnName) {
		if(!tInfo.hasColumnLabel(columnName)) {
			throw new IllegalArgumentException("column [" + columnName + "] not found in " + tInfo.getTableName());
		}
		StringBuilder sql = new StringBuilder("select ");
		boolean first = true;
		for (String column : tInfo.getColumnTypeMap().keySet()) {
			if (!first) {
				sql.append(", ");
			} else {
				first = false;
			}
			sql.append(column.trim());
		}
		sql.append(" from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(columnName).append(" = ?");
		return sql.toString();
	}
	
	@Override
	public String forModelFindIdByColumn(TableInfo tInfo, String columnName) {
		if(!tInfo.hasColumnLabel(columnName)) {
			throw new IllegalArgumentException("column [" + columnName + "] not found in " + tInfo.getTableName());
		}
		String sql = "select " + tInfo.getPrimaryKey().trim() +
				" from " +
				tInfo.getTableName() +
				" where " + columnName + " = ?";
		return sql;
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

	@Override
	public String forModelSelectMax(TableInfo tInfo, String columnName) {
		if(!tInfo.hasColumnLabel(columnName)) {
			throw new IllegalArgumentException("column [" + columnName + "] not found in " + tInfo.getTableName());
		}
		String sql = "select " + "max(" + columnName.trim() + ")" +
				" from " +
				tInfo.getTableName() +
				" where " + columnName + " >= ? and " + columnName + " < ?";
		return sql;
	}


}
