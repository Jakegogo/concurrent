package basesource.anno.check;

/**
 * 检测外键
 * @author Jake
 *
 */
public @interface Foreignkey {
	
	/**
	 * 关联类
	 * @return
	 */
	public Class<?> cls();
	
	/**
	 * 关联类属性
	 * @return
	 */
	public String attr();
	
}
