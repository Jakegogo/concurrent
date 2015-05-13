package basesource.anno;

/**
 * 数字范围
 * @author Jake
 *
 */
public @interface Range {
	
	/**
	 * 最小值
	 * @return
	 */
	public long min();
	
	/**
	 * 最大值
	 * @return
	 */
	public long max();
	
}
