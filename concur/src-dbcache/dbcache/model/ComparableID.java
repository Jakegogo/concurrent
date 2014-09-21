package dbcache.model;

import java.io.Serializable;

/**
 * 可排序的主键
 * <br/>列表或分页时获取的ID是有序的
 * <br/>只定义和复制参与比较的属性,并创建此类匿名对象
 * @see dbcache.model.Sortable<PK>
 * @author Jake
 * @date 2014年9月21日下午8:15:49
 */
public abstract class ComparableID<PK extends Comparable<PK> & Serializable> implements Comparable<PK> {

	@Override
	public abstract int compareTo(PK o);

}
