package dbcache.test;

import lock.ChainLock;
import lock.LockUtils;
import utils.typesafe.extended.MultiSafeType;

import java.util.concurrent.CountDownLatch;

public class TestMultiLock {

	public static void main(String[] args) {

//		while(true) {
		
		final A a = new A();
		final B b = new B();
		final C c = new C();

		final int TEST_LOOP = 10;

		final int TEST_COUNT = 10000;

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

//					try {
//						Thread.sleep(new Random().nextInt(10));
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}

					if( l %2 == 0) {
					
						for (int k = 0;k < TEST_LOOP;k++) {

							ChainLock lock = LockUtils.getLock(a, b);
							lock.lock();
							try {
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
//								System.out.println(Thread.currentThread().getId());
							} finally {
								lock.unlock();
							}

							ct1.countDown();
						}
						
					} else {

						for (int k = 0;k < TEST_LOOP;k++) {
							ChainLock lock = LockUtils.getLock(a, b);
							lock.lock();
							try {

								int i = a.i;
								i += 1;
								a.i = i;

//									try {
//										Thread.sleep(5);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}

								int l = c.k;
								l += 1;
								c.k = l;
//									System.out.println(Thread.currentThread().getId());
							} finally {
								lock.unlock();
							}

							ct1.countDown();
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
