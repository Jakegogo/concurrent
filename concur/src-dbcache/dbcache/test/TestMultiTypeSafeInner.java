package dbcache.test;

import utils.typesafe.extended.MultiSafeActor;
import utils.typesafe.extended.MultiSafeType;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试死锁问题
 */
public class TestMultiTypeSafeInner {

	public static void main(String[] args) {

		final ExecutorService executorService = Executors.newFixedThreadPool(5);
		
//		while(true) {
		
		final A a = new A();
		final B b = new B();

		final int TEST_LOOP = 10;

		final int TEST_COUNT = 100;
		
		final CountDownLatch ct = new CountDownLatch(1);

		final CountDownLatch ct1 = new CountDownLatch(TEST_LOOP * TEST_COUNT);

		for (int j = 0; j < TEST_COUNT;j++) {
			
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

							new MultiSafeActor(executorService, a) {

								@Override
								public void run() {
									int i = a.i;
									i += 1;
									a.i = i;


									new MultiSafeActor(executorService, b) {
										@Override
										public void run() {


//									try {
//										Thread.sleep(3);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}

											int j = b.j;
											j += 1;
											b.j = j;
//									System.out.println(Thread.currentThread().getId());

											ct1.countDown();
										}
									}.start();
								}

							}.start();
						}
						
					} else {

						for (int k = 0;k < TEST_LOOP;k++) {

							new MultiSafeActor(executorService, b) {

								@Override
								public void run() {
									int j = b.j;
									j += 1;
									b.j = j;

//									try {
//										Thread.sleep(5);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}

									new MultiSafeActor(executorService, a) {
										@Override
										public void run() {


//									try {
//										Thread.sleep(3);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}

											int i = a.i;
											i += 1;
											a.i = i;
//									System.out.println(Thread.currentThread().getId());

											ct1.countDown();
										}
									}.start();
								}

							}.start();
						}


						
					}
				}
			}.start();
		}
		
		try {
			long t1 = System.currentTimeMillis();
			ct.countDown();
			ct1.await();
			System.out.println(System.currentTimeMillis() - t1);
			executorService.shutdown();
			System.out.println("a.i:" + a.i);
			System.out.println("b.j:" + b.j);
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
