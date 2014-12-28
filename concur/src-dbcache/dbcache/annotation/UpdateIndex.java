package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体方法更新索引注解
 * @author Jake
 * @date 2014年9月6日上午12:44:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UpdateIndex {

	/**
	 * 索引名数组
	 * @return
	 */
	public String[] value() default {};

}
