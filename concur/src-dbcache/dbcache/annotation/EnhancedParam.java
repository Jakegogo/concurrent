package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ASM代理对象参数
 * <br/> 如equals(@EnhancedParam Object object)
 * Created by Jake on 2015/1/19.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnhancedParam {

}
