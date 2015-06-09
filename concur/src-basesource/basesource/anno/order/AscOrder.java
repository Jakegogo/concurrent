package basesource.anno.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 正序排序
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AscOrder {
	
	/**
	 * 排序名称
	 * @return
	 */
	public String name();
	
}
