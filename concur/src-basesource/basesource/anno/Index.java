package basesource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

/**
 * 静态资源数据索引声明
 * @author frank
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Index {
	
	/** 索引名，同一资源的索引名必须唯一 */
	String name();
	
	/** 索引值是否唯一 */
	boolean unique() default false;

	/** 排序器配置 */
	@SuppressWarnings("rawtypes")
	Class<? extends Comparator> comparatorClz() default Comparator.class;
	
}
