package basesource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import basesource.validators.Validator;

/**
 * 自定义验证注解
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
	
	/**
	 * 自定义验证器
	 * @return
	 */
	public Class<? extends Validator> cls();
	
	/**
	 * 参数
	 * @return
	 */
	public String params();
	
	
	public String msg() default "";
	
}
