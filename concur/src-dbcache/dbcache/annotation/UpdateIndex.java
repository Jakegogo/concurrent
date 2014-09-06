package dbcache.annotation;

/**
 * 指定方法更新索引注解
 * @author Jake
 * @date 2014年9月6日上午12:44:53
 */
public @interface UpdateIndex {
	
	/**
	 * 索引名数组
	 * @return
	 */
	public String[] value() default {};
	
}
