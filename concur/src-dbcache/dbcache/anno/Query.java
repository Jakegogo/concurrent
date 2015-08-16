package dbcache.anno;

/**
 * 带缓存的查询语句注解
 * Created by Jake on 2015/8/1.
 */
public @interface Query {

    /**
     * SQL语句
     * @return
     */
    public String value();

}
