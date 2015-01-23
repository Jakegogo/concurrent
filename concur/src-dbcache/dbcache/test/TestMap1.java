package dbcache.test;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import dbcache.utils.concurrent.ConcurrentHashMap;

public class TestMap1 {

	public static void main(String[] args) {
		final ConcurrentMap<Integer, Integer> map = new ConcurrentHashMap<Integer, Integer>();//jdk8
		
		final ConcurrentMap<Integer, Integer> map1 = new java.util.concurrent.ConcurrentHashMap<Integer, Integer>();//jdk6
		
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
						for(int l = 0;l < 100;l++) {
							map.keySet();
							map.entrySet();
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
						for(int l = 0;l < 100;l++) {
							map.keySet();
							map.entrySet();
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
		
	}
	
}
