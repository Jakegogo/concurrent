package dbcache.persist;

/**
 * 持久化行为接口
 * @author Administrator
 *
 */
public interface PersistAction extends Runnable {

	/**
	 * 是否有效
	 * @return
	 */
	boolean valid();

	/**
	 * 执行持久化操作
	 */
	@Override
	void run();

	/**
	 * 转换成字符串
	 * @return
	 */
	String getPersistInfo();

}
