package dbcache.conf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 内部自动注入
 * @see DbConfigFactory#getDbCacheServiceBean(java.lang.Class<T>)
 * @author Jake
 * @date 2014年9月21日下午4:50:14
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Inject {

}
