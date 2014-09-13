package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引属性注解
 * <br/>同hibernate的@Index
 * @see org.hibernate.annotations.Index
 * @author Jake
 * @date 2014年9月7日下午10:50:17
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {

	/**
	 * 索引名
	 * @return
	 */
	public String name();

}
