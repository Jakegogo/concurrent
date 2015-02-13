package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * json属性注解<br/>
 * json属性定义务必使用线程安全的实现类类型,如ConcurrentHashSet,ConcurrentHashMap。
 * @use fast-json
 * @author Jake
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonType {
	
}
