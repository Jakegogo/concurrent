package dbcache.utils.concurrent;

import dbcache.utils.NamedThreadFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 顺序执行的线程池
 * <br/>顺序执行;同时复用线程,减少线程切换。适用于并发提交
 * <br/> 只能提交LinkingRunnable
 * Created by Jake on 2015/2/1.
 */
public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {

	/**
     * We don't bother to update head or tail pointers if fewer than
     * HOPS links from "true" location. We assume that volatile
     * writes are significantly more expensive than volatile reads.
     */
    private static final int HOPS = 1;

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

	@Override
	public Future<?> submit(Runnable task) {
		execute(task);
		return null;
	}

	@Override
    public void execute(Runnable command) {

        if (!(command instanceof LinkingRunnable)) {
            throw new IllegalArgumentException("只能提交LinkingRunnable.");
        }

        LinkingRunnable runnable = (LinkingRunnable) command;
		appendSubmit(runnable);
	}

	private void appendSubmit(LinkingRunnable runnable) {
		// messages from the same client are handled orderly
		AtomicReference<LinkingRunnable> lastRef = runnable.getLastLinkingRunnable();


//        if (old == null) { // No previous job
//            execs.submit(job);
//        } else {
//            if (old.next.compareAndSet(null, job)) {
//                // successfully append to previous task
//            } else {
//                // previous message is handled, order is guaranteed.
//                execs.submit(job);
//            }
//        }


//      if (lastRef.compareAndSet(null, runnable)) { // No previous job
//			super.execute(command);
//		} else {
//			// CAS loop
//			for (;;) {
//				LinkingRunnable last = lastRef.get();
//				LinkingRunnable next = last.next.get();
//				if (last.next.compareAndSet(null, runnable)) {
//					lastRef.compareAndSet(last, runnable);// fail is OK
//					// successfully append to previous task
//					break;
//				} else if (last.next.get() == last) {
//					// previous message is handled, order is guaranteed.
//					super.execute(command);
//					break;
//				}
//				lastRef = runnable.getLastLinkingRunnable();
//			}
//		}

		if (lastRef.get() == null && lastRef.compareAndSet(null, runnable)) { // No previous job
			super.execute(runnable);
		} else {
			// CAS loop
			for (; ; ) {

				LinkingRunnable last = lastRef.get();

				AtomicReference<LinkingRunnable> nextRef = last.next;

				LinkingRunnable next = nextRef.get();
				if (next != null) {
					if (next == last && lastRef.compareAndSet(last, runnable)) {
						// previous message is handled, order is
						// guaranteed.
						super.execute(runnable);
						return;
					}
				} else if (nextRef.compareAndSet(null, runnable)) {
					lastRef.compareAndSet(last, runnable);// fail is OK
					// successfully append to previous task
					return;
				}
			}
		}
	}


	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		if (t != null) {
			LinkingRunnable runnable = (LinkingRunnable) r;
			runnable.onException(t);
		}
	}


	/**
	 * 创建ExecutorService
	 * 使用默认的AbortPolicy将抛出RejectedExecutionException
	 * @param nThreads
	 * @param threadFactory
	 * @return
	 */
	public static ExecutorService newFixedThreadPool(int nThreads, NamedThreadFactory threadFactory) {
        return new OrderedThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }
    
}
