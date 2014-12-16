package dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引排序字段标注
 * Created by Administrator on 2014/12/16.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderBy {

    /**
     * 对应的索引名,默认为第一个索引
     * @return
     */
    public String index() default "";

    /**
     * 排序优先级,默认1,越小标识优先级越高
     * @return
     */
    public int order() default 1;

}
