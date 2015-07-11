package dbcache.test;

import utils.typesafe.extended.MultiSafeActor;
import utils.typesafe.extended.MultiSafeType;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMultiTypeSafe {

	public static void main(String[] args) {

		final ExecutorService executorService = Executors.newFixedThreadPool(5);
		
//		while(true) {
		
		final A a = new A();
		final B b = new B();
		final C c = new C();
				
		final int TEST_LOOP = 1;
		
		final CountDownLatch ct = new CountDownLatch(1);
		
		for (int j = 0; j < 1000;j++) {
			
			final int l = j;
			new Thread() {
				
				@Override
				public void run() {
					
					try {
						ct.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					try {
						Thread.sleep(new Random().nextInt(10));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if( l %2 == 0) {
					
						for (int k = 0;k < TEST_LOOP;k++) {

							new MultiSafeActor(executorService, a, b) {

								@Override
								public void run() {
									int i = a.i;
									i += 1;
									a.i = i;

//									try {
//										Thread.sleep(3);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}

									int j = b.j;
									j += 1;
									b.j = j;
									System.out.println(Thread.currentThread().getId());
								}

							}.start();
						}
						
					} else {

						for (int k = 0;k < TEST_LOOP;k++) {

							new MultiSafeActor(executorService, a, c) {

								@Override
								public void run() {
									int i = a.i;
									i += 1;
									a.i = i;

//									try {
//										Thread.sleep(5);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}

									int k = c.k;
									k += 1;
									c.k = k;
									System.out.println(Thread.currentThread().getId());
								}

							}.start();
						}
						
					}
				}
			}.start();
		}
		
		try {
			ct.countDown();
			Thread.sleep(2000);
			executorService.shutdown();
			System.out.println("a.i:" + a.i);
			System.out.println("b.j:" + b.j);
			System.out.println("c.k:" + c.k);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		}
		
	}

	static class A extends MultiSafeType {
		
		int i = 0;

		@Override
		public String toString() {
			return "A [i=" + i + "]";
		}
		
	}
	
	static class B extends MultiSafeType {
		
		int j = 0;

		@Override
		public String toString() {
			return "B [j=" + j + "]";
		}
		
	}
	
	static class C extends MultiSafeType {
		
		int k = 0;

		@Override
		public String toString() {
			return "C [k=" + k + "]";
		}
		
	}
	
}
