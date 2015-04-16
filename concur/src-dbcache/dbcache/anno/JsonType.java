package dbcache.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * json属性注解<br/>
 * <br/>可达到json字段自动转换存储
 * 对应的临时属性的定义需使用线程安全的实现类类型,如ConcurrentHashSet,ConcurrentHashMap。
 * 需要同时实体@Transient或@Column(..)
 * @use fast-json
 * @author Jake
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonType {
	
}
