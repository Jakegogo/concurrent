package dbcache.test;

import dbcache.utils.typesafe.SafeActor;
import dbcache.utils.typesafe.SafeType;

import java.util.concurrent.CountDownLatch;

public class TestTypeSafe {
	
	public static void main(String[] args) {
		
		final A a = new A();
		final B b = new B();
		final C c = new C();
				
		final int TEST_LOOP = 1;
		
		final CountDownLatch ct = new CountDownLatch(1);
		
		for (int j = 0; j < 4;j++) {
			
			final int l = j;
			new Thread() {
				
				@Override
				public void run() {
					
					try {
						ct.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if( l < 2) {
					
						for (int k = 0;k < TEST_LOOP;k++) {
							
							new SafeActor(a, b) {
								
								@Override
								public void run() {
									int i = a.i;
									i += 1;
									a.i = i;
									
									int j = b.j;
									j += 1;
									b.j = j;
									
								}
								
							}.start();
						}
						
					} else {
						
						for (int k = 0;k < TEST_LOOP;k++) {
							
							new SafeActor(a, c) {
								
								@Override
								public void run() {
									int i = a.i;
									i += 1;
									a.i = i;
									
									int k = c.k;
									k += 1;
									c.k = k;
									
								}
								
							}.start();
						}
						
					}
				}
			}.start();
		}
		
		try {
			ct.countDown();
			Thread.sleep(3000);
			System.out.println("a.i:" + a.i);
			System.out.println("b.j:" + b.j);
			System.out.println("c.k:" + c.k);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	static class A extends SafeType {
		
		int i = 0;
		
	}
	
	static class B extends SafeType {
		
		int j = 0;
		
	}
	
	static class C extends SafeType {
		
		int k = 0;
		
	}
	
}
