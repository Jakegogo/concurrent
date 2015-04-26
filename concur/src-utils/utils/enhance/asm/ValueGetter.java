package utils.enhance.asm;


/**
 * 值获取器接口
 * @author Jake
 * @date 2014年11月2日下午6:33:26
 */
public interface ValueGetter<T> {

	/**
	 * 获取值
	 * @param target 目标实体
	 * @return 返回值
	 */
	public Object get(T target);

	/**
	 * 获取值的名称
	 * @return
	 */
	public String getName();

	/**
	 * 克隆对象
	 * @return
	 */
	public ValueGetter<T> doClone();

}
