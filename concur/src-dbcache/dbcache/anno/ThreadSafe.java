package dbcache.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注是否线程安全
 * <br/>不纳入编译
 * @author Jake
 * @date 2014年9月13日下午1:30:16
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface ThreadSafe {

	/**
	 * 标注内部持有锁对象的类型
	 * @return
	 */
	public Class<?>[] guardBy() default void.class;

}
