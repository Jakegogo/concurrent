package dbcache.key.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 根据服Id创建唯一主键
 * 
 * @see dbcache.key.annotation.IdGenerate
 * @author jake
 *
 */
@Target(FIELD) 
@Retention(RUNTIME)
public @interface Id {
	
}
