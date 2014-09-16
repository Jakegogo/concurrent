package dbcache.key;

import java.util.UUID;


/**
 * String主键id生成器
 * 不建议试用String作为主键,hash碰撞较为严重
 */
public class StringGenerator implements IdGenerator<String> {

	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}

}
