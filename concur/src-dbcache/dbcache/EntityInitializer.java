/**
 * 
 */
package dbcache;

/**
 * 实体初始化回调
 * @author jake
 * @date 2014-7-31-下午9:21:33
 */
public interface EntityInitializer {
	
	/**
	 * 实现此方法做一些初始化
	 */
	void doAfterLoad();
	
	/**
	 * 持久化前操作
	 */
	void doBeforePersist();
	
}
