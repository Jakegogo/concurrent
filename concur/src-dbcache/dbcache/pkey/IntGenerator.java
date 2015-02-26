package dbcache.pkey;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * int主键id生成器
 */
public class IntGenerator implements IdGenerator<Integer> {
	
	private AtomicInteger idGenerator;
	
	
	public IntGenerator() {
		this(0);
	}
	
	/**
	 * 构造函数
	 * @param currValue 当前值
	 */
	public IntGenerator(int currValue) {
		idGenerator = new AtomicInteger(currValue);
	}
	

	@Override
	public Integer generateId() {
		return idGenerator.incrementAndGet();
	}

}
