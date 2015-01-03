package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * json属性转换注解<br/>
 * 配合@Transient使用<br/>
 * json属性定义务必使用实现类类型,如ConcurrentHashSet,ConcurrentHashMap。
 * @use fast-json
 * @author Jake
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonConvert {
	
	/**
	 * 指定json串属性名
	 * @return
	 */
	public String value();
	
}
