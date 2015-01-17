package dbcache.utils;

/**
 * 可变Integer
 * 
 * @author Jake
 *
 */
public class MutableInteger {
	private int val;

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

	// 为了方便打印
	public String toString() {
		return Integer.toString(val);
	}
}
