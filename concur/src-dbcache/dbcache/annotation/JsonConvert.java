package dbcache.annotation;

/**
 * json属性转换注解
 * 配合@Transient使用
 * @use fast-json
 * @author Jake
 */
public @interface JsonConvert {
	
	/**
	 * 指定json串属性名
	 * @return
	 */
	public String property();
	
}
