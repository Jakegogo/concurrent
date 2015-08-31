package dbcache.conf;


/**
 * 分表策略接口
 * @author Jake
 */
public interface ShardStrategy<PK> {
	
	/**
	 * 获取表名后缀
	 * @param key 实体Key
	 * @return
	 */
	String getTableNameSuffix(PK key);
	
	/**
	 * 是否自动建表
	 * @see ShardStrategy#createTableSqlTemplate()
	 * @return
	 */
	boolean autoCreateTable();
	
	/**
	 * 建表语句模版
	 * <br/> 使用占位符"${TableName}"代表表名
	 * @see ShardStrategy#autoCreateTable()
	 * @return
	 */
	String createTableSqlTemplate();
	
}
