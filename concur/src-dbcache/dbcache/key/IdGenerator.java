package dbcache.key;

import java.io.Serializable;

/**
 * 主键id生成器接口
 */
public interface IdGenerator<PK extends Serializable> {

	/**
	 * 生成id
	 * @return PK
	 */
	PK generateId();
	
}
