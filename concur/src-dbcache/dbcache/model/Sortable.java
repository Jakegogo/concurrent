package dbcache.model;

import java.io.Serializable;

/**
 * 可排序的
 * <br/>列表或分页时获取的内容是有序的
 * @author Jake
 * @date 2014年9月21日下午8:20:52
 */
public interface Sortable<PK extends Comparable<PK> & Serializable> {

	/**
	 * 获取可排序的ID
	 * @return
	 */
	public ComparableID<PK> getComparableID();

}
