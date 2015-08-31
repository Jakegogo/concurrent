package basesource.anno.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Contains {
	
	/**
	 * 错误提示信息
	 * @return
	 */
	String msg() default "";
	
	/**
	 * 按字段分组
	 * @return
	 */
	String[] groupBy() default {};
	
	/**
	 * 外键的类
	 * @return
	 */
	Class<?>[] foreignClass() default {};
	
	/**
	 * 外键的类
	 * @return
	 */
	String[] foreignKey() default {};
	
}
