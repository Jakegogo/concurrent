package dbcache.key;

import java.util.UUID;


/**
 * String主键id生成器
 */
public class StringGenerator implements IdGenerator<String> {

	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}

}
