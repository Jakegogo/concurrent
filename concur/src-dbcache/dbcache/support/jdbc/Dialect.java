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


import dbcache.support.jdbc.dialect.MysqlDialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Dialect.
 */
public abstract class Dialect {

	public abstract String forTableInfoBuilderDoBuildTableInfo(String tableName);
	public abstract void forSave(TableInfo tableInfo, Map<String, Object> attrs, StringBuilder sql);
	public abstract String forDeleteById(TableInfo tInfo);
	public abstract void forUpdate(TableInfo tableInfo, Map<String, Object> attrs, Set<String> modifyFlag, String pKey, StringBuilder sql);
	public abstract String forFindById(TableInfo tInfo, String columns);
	public abstract String forFindByColumn(TableInfo tInfo, String columns, String columnName);
	public abstract void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect);
	
	public boolean isOracle() {
		return false;
	}
	

	public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		for (int i=0, size=paras.size(); i<size; i++) {
			pst.setObject(i + 1, paras.get(i));
		}
	}
	
	public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		for (int i=0; i<paras.length; i++) {
			pst.setObject(i + 1, paras[i]);
		}
	}
	
	public String getDefaultPrimaryKey() {
		return "id";
	}

	public static Dialect getDefaultDialect() {
		return new MysqlDialect();
	}

}







