package basesource.storage;

import java.util.Comparator;

/**
 * 索引值获取器接口
 * @author frank
 */
public interface IndexGetter {

	/**
	 * 获取索引名
	 * @return
	 */
	String getName();
	
	/**
	 * 是否唯一值索引
	 * @return
	 */
	boolean isUnique();

	/**
	 * 获取索引值
	 * @param obj 静态资源实例
	 * @return 索引值
	 */
	Object getValue(Object obj);
	
	/**
	 * 获取索引排序器
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Comparator getComparator();
	
	/**
	 * 检查是否存在索引排序器
	 * @return
	 */
	boolean hasComparator();
}
