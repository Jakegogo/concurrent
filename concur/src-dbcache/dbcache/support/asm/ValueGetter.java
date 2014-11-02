package dbcache.support.asm;


/**
 * 值获取器接口
 * @author Jake
 * @date 2014年11月2日下午6:33:26
 */
public interface ValueGetter<T> {

	/**
	 * 获取值
	 * @param object 目标对象
	 * @return 返回值
	 */
	public Object get(T object);

	/**
	 * 获取值的名称
	 * @return
	 */
	public String getName();

}
