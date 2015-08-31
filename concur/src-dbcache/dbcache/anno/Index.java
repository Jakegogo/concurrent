package dbcache.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性索引注解
 * <br/>同hibernate的@Index
 * <br/>使用index服务要注意:get出来的Entity对象为代理对象,实现Entity的equals方法时，
 * <br/>由于才有字节码增强监听属性的变化的方式,启用索引后，应避免使用诸如"getClass()"(可使用instanceof);"this.id == that.id"(可使用this.id == this.getId())
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
	String name();

}
