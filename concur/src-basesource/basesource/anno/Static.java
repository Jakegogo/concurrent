package basesource.anno;

import java.beans.PropertyEditor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 静态资源注入注释
 * @author frank
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Static {

	/**
	 * 标识值
	 * @return
	 */
	String value() default "";

	/**
	 * 唯一标识转换器声明
	 * @return
	 */
	Class<? extends PropertyEditor> converter() default PropertyEditor.class;
	
	/**
	 * 注入值是否必须
	 * @return
	 */
	boolean required() default true;
	
	/** 标识值是否唯一值 */
	boolean unique() default false;
}
