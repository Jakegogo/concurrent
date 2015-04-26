package dbcache.utils;

/**
 * 可变Integer
 * 
 * @author Jake
 *
 */
public class MutableInteger {

	private int val = 0;

	public MutableInteger(){}

	public MutableInteger(int val) {
		this.val = val;
	}

	public int get() {
		return this.val;
	}

	public void set(int val) {
		this.val = val;
	}

	public int incrementAndGet() {
		return ++this.val;
	}

	public int getAndIncrement() {
		return this.val++;
	}

	public void increment() {
		this.val++ ;
	}

	// 为了方便打印
	public String toString() {
		return Integer.toString(val);
	}
}
