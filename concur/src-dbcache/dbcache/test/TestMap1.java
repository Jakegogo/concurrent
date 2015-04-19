package dbcache.test;

import dbcache.utils.concurrent.ConcurrentHashMapV8;
import dbcache.utils.concurrent.ConcurrentLinkedHashMap8;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class TestMap1 {

	public static void main(String[] args) {
		final ConcurrentMap<Integer, Integer> map = new ConcurrentHashMapV8<Integer, Integer>();//jdk8
		
		final ConcurrentMap<Integer, Integer> map1 = new java.util.concurrent.ConcurrentHashMap<Integer, Integer>();//jdk6

		final ConcurrentMap<Integer, Integer> map2 = new ConcurrentLinkedHashMap8<Integer, Integer>();//linked
		
		final CountDownLatch ct1 = new CountDownLatch(1);
		final CountDownLatch ct2 = new CountDownLatch(16);
		
		for(int t = 0;t < 16;t++) {
			new Thread() {
				public void run() {
					try {
						ct1.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
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
						for(Integer key : map.keySet()) {
							;
						}
						for(Map.Entry<Integer, Integer> key : map.entrySet()) {
							;
						}
					}
					
					ct2.countDown();
			
				}
			}.start();
		}
		
		long t1 = System.currentTimeMillis();
		ct1.countDown();
		try {
			ct2.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - t1);
		
		
		final CountDownLatch ct3 = new CountDownLatch(1);
		final CountDownLatch ct4 = new CountDownLatch(16);
		
		for(int t = 0;t < 16;t++) {
			new Thread() {
				public void run() {
					try {
						ct3.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
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
						for(Integer key : map1.keySet()) {
							;
						}
						for(Map.Entry<Integer, Integer> key : map1.entrySet()) {
							;
						}
					}
				
				
					ct4.countDown();
			
				}
			}.start();
		}
		
		t1 = System.currentTimeMillis();
		ct3.countDown();
		try {
			ct4.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - t1);


		final CountDownLatch ct5 = new CountDownLatch(1);
		final CountDownLatch ct6 = new CountDownLatch(16);

		for(int t = 0;t < 16;t++) {
			new Thread() {
				public void run() {
					try {
						ct5.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					for(int i = 1; i <= 100000;i++) {

						for(int k = 0;k < 50;k++) {
							map2.putIfAbsent(i, i);
						}
						map2.put(i, i);
						for(int j = 0;j < 1000;j++) {
							map2.get(i);
						}
						for(int k = 0;k < 50;k++) {
							map2.putIfAbsent(i, i);
						}
						map2.remove(i);
						for(Integer key : map2.keySet()) {
							;
						}
						for(Map.Entry<Integer, Integer> key : map2.entrySet()) {
							;
						}
					}


					ct6.countDown();

				}
			}.start();
		}

		t1 = System.currentTimeMillis();
		ct5.countDown();
		try {
			ct6.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - t1);

		
	}
	
}
