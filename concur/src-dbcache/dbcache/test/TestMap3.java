package dbcache.test;

import utils.collections.concurrent.ConcurrentHashMapV8;
import utils.collections.concurrent.ConcurrentLinkedHashMap8;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class TestMap3 {

	public static void main(String[] args) {
		ConcurrentMap<Integer, Integer> map = new ConcurrentHashMapV8<Integer, Integer>();//jdk8

		ConcurrentMap<Integer, Integer> map1 = new java.util.concurrent.ConcurrentHashMap<Integer, Integer>();//jdk6

		ConcurrentMap<Integer, Integer> map2 = new ConcurrentLinkedHashMap8<Integer, Integer>();//jdk6

		for (int i = 1; i <= 100000; i++) {

			for (int k = 0; k < 1; k++) {
				map.putIfAbsent(i, i);
			}
			for(int k = 0;k < 1;k++) {
				map1.putIfAbsent(i, i);
			}
			for(int k = 0;k < 1;k++) {
				map2.putIfAbsent(i, i);
			}
		}
		long t1 = System.currentTimeMillis();
		for(int i = 1; i <= 10000;i++) {

//			map.put(i, i);
//			for(int j = 0;j < 1000;j++) {
//				map.get(i);
//			}
//			for(int k = 0;k < 50;k++) {
//				map.putIfAbsent(i, i);
//			}
//			map.remove(i);
			for(int l = 0;l < 1;l++) {
				for(Integer key : map.keySet()) {
					;
				}
				for(Map.Entry<Integer, Integer> key : map.entrySet()) {
					;
				}
			}
		}
		System.out.println(System.currentTimeMillis() - t1);


		t1 = System.currentTimeMillis();
		for(int i = 1; i <= 10000;i++) {


//			map1.put(i, i);
//			for(int j = 0;j < 1000;j++) {
//				map1.get(i);
//			}
//			for(int k = 0;k < 50;k++) {
//				map1.putIfAbsent(i, i);
//			}
//			map1.remove(i);
			for(int l = 0;l < 1;l++) {
				for(Integer key : map1.keySet()) {
					;
				}
				for(Map.Entry<Integer, Integer> key : map1.entrySet()) {
					;
				}
			}
		}
		System.out.println(System.currentTimeMillis() - t1);



		t1 = System.currentTimeMillis();
		for(int i = 1; i <= 10000;i++) {


//			map2.put(i, i);
//			for(int j = 0;j < 1000;j++) {
//				map2.get(i);
//			}
//			for(int k = 0;k < 50;k++) {
//				map2.putIfAbsent(i, i);
//			}
//			map2.remove(i);
			for(int l = 0;l < 1;l++) {
				for(Integer key : map2.keySet()) {
					;
				}
				for(Map.Entry<Integer, Integer> key : map2.entrySet()) {
					;
				}

			}
		}
		System.out.println(System.currentTimeMillis() - t1);


	}

}
