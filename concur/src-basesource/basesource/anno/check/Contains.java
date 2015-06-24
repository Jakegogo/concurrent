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
	public String msg() default "";
	
	/**
	 * 按字段分组
	 * @return
	 */
	public String[] groupBy() default {};
	
	/**
	 * 外键的类
	 * @return
	 */
	public Class<?>[] foreignClass() default {};
	
	/**
	 * 外键的类
	 * @return
	 */
	public String[] foreignKey() default {};
	
}
