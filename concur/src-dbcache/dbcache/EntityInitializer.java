/**
 * 
 */
package dbcache;

/**
 * 实体回调接口
 * <br/>适用于实体类
 * @author jake
 * @date 2014-7-31-下午9:21:33
 */
public interface EntityInitializer {
	
	/**
	 * 加载Bean后回调
	 */
	void doAfterLoad();
	
	/**
	 * 持久化前回调
	 */
	void doBeforePersist();
	
}
