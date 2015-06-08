package basesource.anno.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import basesource.contants.ValueType;
import basesource.validators.ValueGetter;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Contains {
	
	/**
	 * 错误提示信息
	 * @return
	 */
	public String msg() default "";
	
	/**
	 * 值(json格式的值使用:{value}.xxx.xx)
	 * @return
	 */
	public String[] value() default ValueType.VALUE;
	
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
	
	/**
	 * 获取值的类
	 * @return
	 */
	public Class<? extends ValueGetter> valueGetter() default ValueGetter.class;

}
