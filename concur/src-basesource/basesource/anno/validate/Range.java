package basesource.anno.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数字范围
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
	
	/**
	 * 最小值
	 * @return
	 */
	public long min();
	
	/**
	 * 最大值
	 * @return
	 */
	public long max();
	
	/**
	 * 错误提示信息
	 * @return
	 */
	public String msg() default "";
	
}
