package basesource.convertor.utils;

/**
 * 类头信息
 */
public interface ClassMeta {

	/**
	 * 获取类名
	 * @return
	 */
	String getClassName();

	/**
	 * 获取类版本
	 * @return
	 */
	int getVersion();

	/**
	 * 获取类标识
	 * @return
	 */
	int getAccess();

	/**
	 * 获取超类名称
	 * @return
	 */
	String getSuperName();

	/**
	 * 获取接口数组
	 * @return
	 */
	String[] getInterfaces();

	/**
	 * 获取类签名
	 * @return
	 */
	String getSignature();

	/**
	 * 获取类文件字节数组
	 * @return
	 */
	byte[] getBytes();
}
