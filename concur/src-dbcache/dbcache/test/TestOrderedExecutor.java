package dbcache.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import dbcache.utils.NamedThreadFactory;
import dbcache.utils.executor.LinkingRunnable;
import dbcache.utils.executor.OrderedThreadPoolExecutor;

public class TestOrderedExecutor {
	
	public static void main(String[] args) {
		
		// 初始化入库线程
		ThreadGroup threadGroup = new ThreadGroup("TestOrderedExecutor组");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "测试TestOrderedExecutor线程池");


		final IntegerRefrence i = new IntegerRefrence(0);
		// 初始化线程池
		final ExecutorService executorService = OrderedThreadPoolExecutor.newFixedThreadPool(4, threadFactory);
		
		final AtomicReference<LinkingRunnable> last = new AtomicReference<LinkingRunnable>();
		
		final int TEST_LOOP = 1000000;
		
		final CountDownLatch ct = new CountDownLatch(1);
		
		for (int j = 0; j < 4;j++) {
			
			new Thread() {
			
				@Override
				public void run() {
					
					try {
						ct.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					for (int k = 0;k < TEST_LOOP;k++) {
						executorService.execute(new LinkingRunnable() {
							
							@Override
							public AtomicReference<LinkingRunnable> getLastLinkingRunnable() {
								return last;
							}
							
							@Override
							public void run() {
								int j = i.getVal();
	
								j+=1;
	
								i.setVal(j);
							}
							
						});
					}
				}
			}.start();
		}
		
		try {
			ct.countDown();
			Thread.sleep(3000);
			System.out.println(i.getVal());
			executorService.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	static class IntegerRefrence {
		
		int val;
		
		public int getVal() {
			return val;
		}

		public void setVal(int val) {
			this.val = val;
		}

		public IntegerRefrence(int val) {
			this.val = val;
		}
		
		
	}
	
}
