package dbcache.pkey;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Long主键id生成器
 */
public class LongGenerator implements IdGenerator<Long> {
	
	private final AtomicLong idGenerator;
	
	
	public LongGenerator() {
		this(0L);
	}
	
	
	/**
	 * 构造函数
	 * @param currValue 当前值
	 */
	public LongGenerator(long currValue) {
		idGenerator = new AtomicLong(currValue);
	}
	

	@Override
	public Long generateId() {
		return idGenerator.incrementAndGet();
	}

}
