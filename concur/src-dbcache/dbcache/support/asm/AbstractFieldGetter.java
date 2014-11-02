package dbcache.support.asm;

/**
 * 属性获取器
 * @author Jake
 * @date 2014年11月2日下午2:11:00
 * @param T 实体类型
 */
public abstract class AbstractFieldGetter<T> implements ValueGetter<T> {

	/**
	 * 获取属性值
	 * @param object 目标对象
	 * @return 属性值
	 */
	public abstract Object get(T object);


}
