package dbcache.test;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import dbcache.utils.ConcurrentHashMap;

public class TestMap {

	public static void main(String[] args) {
		ConcurrentMap<Integer, Integer> map = new ConcurrentHashMap<Integer, Integer>();//jdk8
		
		ConcurrentMap<Integer, Integer> map1 = new java.util.concurrent.ConcurrentHashMap<Integer, Integer>();//jdk6
		
		long t1 = System.currentTimeMillis();
		for(int i = 1; i <= 100000;i++) {
			
			for(int k = 0;k < 50;k++) {
				map.putIfAbsent(i, i);
			}
			map.put(i, i);
			for(int j = 0;j < 1000;j++) {
				map.get(i);
			}
			for(int k = 0;k < 50;k++) {
				map.putIfAbsent(i, i);
			}
			map.remove(i);
			for(int l = 0;l < 100;l++) {
				map.keySet();
				map.entrySet();
			}
		}
		System.out.println(System.currentTimeMillis() - t1);
		
		
		t1 = System.currentTimeMillis();
		for(int i = 1; i <= 100000;i++) {
			
			for(int k = 0;k < 50;k++) {
				map1.putIfAbsent(i, i);
			}
			map1.put(i, i);
			for(int j = 0;j < 1000;j++) {
				map1.get(i);
			}
			for(int k = 0;k < 50;k++) {
				map1.putIfAbsent(i, i);
			}
			map1.remove(i);
			for(int l = 0;l < 100;l++) {
				map.keySet();
				map.entrySet();
			}
		}
		System.out.println(System.currentTimeMillis() - t1);
		
	}
	
}
