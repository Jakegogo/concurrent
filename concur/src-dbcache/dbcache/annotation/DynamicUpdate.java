package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态更新字段注解
 * 用于大字段动态update,无修改字段值则不更新入库
 * @date 2015年1月12日下午11:52:50
 * @author Jake
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicUpdate {
	
}
