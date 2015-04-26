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

import java.util.Collection;


/**
 * SqliteDialect.
 */
public class Sqlite3Dialect extends Dialect {
	
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
		sql.append(" where 1 = 2");
		return sql.toString();
	}
	
	public void forModelSave(TableInfo tableInfo, StringBuilder sql) {
		sql.append("insert into ").append(tableInfo.getTableName()).append("(");
		StringBuilder temp = new StringBuilder(") values(");
		boolean first = true;
		for (String colName : tableInfo.getColumnNames()) {
			if (!first) {
				sql.append(", ");
				temp.append(", ");
			} else {
				first = false;
			}
			sql.append(colName);
			temp.append("?");
		}
		sql.append(temp.toString()).append(")");
	}
	
	public String forModelDeleteById(TableInfo tInfo) {
		String pKey = tInfo.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(pKey).append(" = ?");
		return sql.toString();
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
		StringBuilder sql = new StringBuilder("select ");
		sql.append(tInfo.getPrimaryKey().trim());
		sql.append(" from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(columnName).append(" = ?");
		return sql.toString();
	}

	public void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		int offset = pageSize * (pageNumber - 1);
		sql.append(select).append(" ");
		sql.append(sqlExceptSelect);
		sql.append(" limit ").append(offset).append(", ").append(pageSize);
	}

	@Override
	public String forModelSelectMax(TableInfo tInfo, String columnName) {
		if(!tInfo.hasColumnLabel(columnName)) {
			throw new IllegalArgumentException("column [" + columnName + "] not found in " + tInfo.getTableName());
		}
		StringBuilder sql = new StringBuilder("select max(");
		sql.append(columnName.trim());
		sql.append(") from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(columnName).append(" >= ? and ").append(columnName).append(" < ?");
		return sql.toString();
	}


}
