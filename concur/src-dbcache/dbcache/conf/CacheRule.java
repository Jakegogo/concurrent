package dbcache.conf;


/**
 * 缓存规则合集
 * @author Jake
 * @date 2014年8月13日上午1:15:55
 */
public class CacheRule {

	/**
	 * 获取索引Key
	 * @param indexName 索引名
	 * @param indexValue 索引值
	 * @return
	 */
	public static Integer getIndexIdKey(String indexName, Object indexValue) {
		return Integer.valueOf((17 + indexName.hashCode()) * 37 + indexValue.hashCode());
	}

}
