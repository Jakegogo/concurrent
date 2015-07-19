package dbcache;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;


/**
 * 通用实体基类
 * <br/>建议实体类继承此类,或者实现equals()和hashCode()方法
 * @param <PK & Serializable> 主键ID类型
 */
public abstract class BaseEntity<PK extends Comparable<PK> & Serializable> implements IEntity<PK>, Serializable{

	/** */
	private static final long serialVersionUID = -8011061374263995942L;


	/**
	 * Get id
	 * @return id
	 */
    public abstract PK getId();


    /**
     * Set Id
     * @param id
     */
    public abstract void setId(PK id);


    /**
     * Returns a multi-line String with key=value pairs.
     * @return a String representation of this class.
     */
    public String toString() {
    	return ReflectionToStringBuilder.toString(this);
    }


    /**
     * Compares object equality. When using Hibernate, the primary key should
     * not be a part of this comparison.
     * @param o object to compare to
     * @return true/false based on equality tests
     */
    @SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof BaseEntity)) {
            return false;
		}

		if (o.getClass() != getClass()) {
			return false;
		}

		BaseEntity rhs = (BaseEntity) o;

		if(this.getId() == null || rhs.getId() == null) {
			return false;
		}

		return this.getId().equals(rhs.getId());
    }


    /**
     * When you override equals, you should override hashCode. See "Why are
     * equals() and hashCode() importation" for more information:
     * http://www.hibernate.org/109.html
     * @return hashCode
     */
    public int hashCode() {
    	return 305668771 + 1793910479 * this.getId().hashCode();
    }


    /**
	 * 获取实体标识
	 * @return PK
	 */
	public PK getIdentity() {
		return getId();
	}


}
