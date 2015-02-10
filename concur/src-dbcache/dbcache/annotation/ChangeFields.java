package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体方法修改注解
 * <br/> 标注该方法将会修改到对应的索引属性value[]的值
 * @author Jake
 * @date 2014年9月6日上午12:44:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChangeFields {

	/**
	 * 属性名数组
	 * @return
	 */
	public String[] value() default {};

}
