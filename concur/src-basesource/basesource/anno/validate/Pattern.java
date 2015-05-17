package basesource.anno.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 正则表达式(不包括0)
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Pattern {
	
	public String msg() default "";
	
	
	/**
	 * 值(json格式的值使用:value.xxx.xx)
	 * @return
	 */
	public String value() default "value";
	
}
