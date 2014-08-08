package dbcache.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;


/**
 * 实体对象基类
 * @param <PK> 主键ID类型
 * @param <Serializable> 
 */
public abstract class BaseModel<PK extends Comparable<PK> & Serializable> implements IEntity<PK>, Serializable{
	
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
		
		if (!(o instanceof BaseModel)) {
            return false;
		}
		
		if (o.getClass() != getClass()) {
			return false;
		}
		
		BaseModel rhs = (BaseModel) o;
		return new EqualsBuilder()
							.append(this.getId(), rhs.getId())
							.isEquals();
    }

    /**
     * When you override equals, you should override hashCode. See "Why are
     * equals() and hashCode() importation" for more information:
     * http://www.hibernate.org/109.html
     * @return hashCode
     */
    public int hashCode() {
    	return new HashCodeBuilder(305668771, 1793910479)
								.append(this.getId())
								.toHashCode();
    }
    
    
    /**
	 * 获取实体标识
	 * @return PK
	 */
	public PK getIdentity() {
		return getId();
	}
    
    
}
