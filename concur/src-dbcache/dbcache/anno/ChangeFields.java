package dbcache.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体方法修改注解
 * <br/> 由于采用字节码增强实现监听属性变化的方式,外部put/add瞬时的如Map,List等结构时的属性,需要在update之前再次set一遍,或者加上次注解代替
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
	String[] value() default {};

}
