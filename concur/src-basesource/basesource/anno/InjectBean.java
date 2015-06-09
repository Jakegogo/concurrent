package basesource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Spring 容器内的 Bean 注入到静态资源类实例的注释
 * @author frank
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectBean {

	/** 被注入的 Bean 标识(默认按类型进行注入) */
	String value() default "";
	
}
