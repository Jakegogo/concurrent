package dbcache.utils;

/**
 * 整数计数器
 * 
 * @author Jake
 *
 */
public class IntegerCounter {
	
	private int value = 0;
	
	public IntegerCounter() {
	}
	
	public IntegerCounter(int init) {
		this.value = init;
	}
	
	public int incrementAndGet() {
		return ++this.value;
	}
	
	public int getAndIncrement() {
		return this.value++;
	}
	
	public void increment() {
		this.value++ ;
	}
	
	public int get() {
		return this.value;
	}
	
	public void reset() {
		this.value = 0;
	}
	
}
