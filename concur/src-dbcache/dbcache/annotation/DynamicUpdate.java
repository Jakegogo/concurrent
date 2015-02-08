package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态更新字段注解
 * <br/>用于大字段动态update,无修改字段值则不更新入库
 * <br/>通过调用setXX()方法可以检测到修改的属性
 * <br/>或者使用@ChangeFields标注方法将要修改到的属性
 * @date 2015年1月12日下午11:52:50
 * @author Jake
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicUpdate {
	
}
