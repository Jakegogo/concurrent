package dbcache.test;

import dbcache.utils.ConcurrentHashMap;

public class TestMap2 {

	public static void main(String[] args) {
		ConcurrentHashMap.initProbe();
		long t1 = System.currentTimeMillis();
		for( int i = 0;i < 100000000;i++) {
			
			
			int j = ConcurrentHashMap.getProbe();
			ConcurrentHashMap.setProbe(j);
		}
		System.out.println(System.currentTimeMillis() - t1);
	}
	
}
