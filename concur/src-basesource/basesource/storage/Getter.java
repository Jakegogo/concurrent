package basesource.storage;

/**
 * 唯一标示获取接口
 * @author frank
 */
public interface Getter {

	/**
	 * 获取 Getter 值
	 * @param obj 静态资源实例
	 * @return 值
	 */
	Object getValue(Object value);
	
}
