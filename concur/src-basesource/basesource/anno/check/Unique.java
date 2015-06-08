package basesource.anno.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import basesource.contants.ValueType;
import basesource.validators.ValueGetter;

/**
 * 唯一
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {
	
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
	 * 获取值的类
	 * @return
	 */
	public Class<? extends ValueGetter> valueGetter() default ValueGetter.class;
	
}
