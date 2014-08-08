package dbcache.key.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 根据服Id创建唯一主键
 * @see dbcache.key.annotation.Id
 * @author jake
 *
 */
@Target(TYPE) 
@Retention(RUNTIME)
public @interface IdGenerate {
	
	/**
	 * 表名
	 * @return
	 */
	public String table() default "";
	
}
