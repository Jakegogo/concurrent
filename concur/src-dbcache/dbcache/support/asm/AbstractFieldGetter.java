package dbcache.support.asm;

/**
 * 属性获取器
 * @author Jake
 * @date 2014年11月2日下午2:11:00
 * @param T 实体类型
 */
public abstract class AbstractFieldGetter<T> implements ValueGetter<T> {

	@Override
	public abstract Object get();


	@Override
	public abstract void setTarget(T object);


	@Override
	public abstract String getName();



}
