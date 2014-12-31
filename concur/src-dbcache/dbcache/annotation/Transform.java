package dbcache.annotation;

/**
 * json属性转换注解
 * @use fast-json
 * @author Jake
 */
public @interface Transform {
	
	/**
	 * 指定json串属性名
	 * @return
	 */
	public String property();
	
}
