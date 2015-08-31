package basesource.anno.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 最大值限制
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Max {
	
	/**
	 * 最大值
	 * @return
	 */
	long max();
	
	/**
	 * 错误提示信息
	 * @return
	 */
	String msg() default "";
	
}
