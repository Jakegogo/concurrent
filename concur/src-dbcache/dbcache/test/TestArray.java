package dbcache.test;

import java.lang.reflect.Array;
import java.util.Arrays;

public class TestArray {
	
	public static void main(String[] args) {
		Object[] intArray = (Object[]) Array.newInstance(Integer.class, 1);
		intArray[0] = 1;
		System.out.println(Arrays.toString(intArray));
	}

}
