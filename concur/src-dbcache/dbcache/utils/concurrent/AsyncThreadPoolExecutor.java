package dbcache.utils.concurrent;

import dbcache.utils.NamedThreadFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 顺序执行的线程池
 * <br/>顺序执行;同时复用线程,减少线程切换。适用于单线程提交
 * <br/> 只能提交LinkingRunnable
 * Created by Jake on 2015/2/1.
 */
public class AsyncThreadPoolExecutor extends ThreadPoolExecutor {

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {

        if (!(command instanceof LinkingRunnable)) {
            throw new IllegalArgumentException("只能提交LinkingRunnable.");
        }

        LinkingRunnable runnable = (LinkingRunnable) command;

        // messages from the same client are handled orderly
        AtomicReference<LinkingRunnable> lastRef = runnable.getLastLinkingRunnable();
        LinkingRunnable last = lastRef.get();
        lastRef.set(runnable);
        
        if (last == null) { // No previous job
            super.submit(runnable);
        } else {
            if (last.next.compareAndSet(null, runnable)) {
                // successfully append to previous task
            } else {
                // previous message is handled, order is guaranteed.
            	super.submit(runnable);
            }
        }
        
        
    }


    public static ExecutorService newFixedThreadPool(int nThreads, NamedThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }
    
}
