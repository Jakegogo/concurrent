package dbcache.test;

import java.lang.reflect.Array;

public class TestArray {
	
	public static void main(String[] args) {
		Object[] intArray = (Object[]) Array.newInstance(Integer.class, 1);
		intArray[0] = Integer.valueOf(1);
		System.out.println(intArray);
	}

}
