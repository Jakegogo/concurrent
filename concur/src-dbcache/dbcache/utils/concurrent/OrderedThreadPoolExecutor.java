package dbcache.utils.concurrent;

import dbcache.utils.NamedThreadFactory;

import java.util.concurrent.*;

/**
 * 顺序执行的线程池
 * <br/> 只能提交LinkingRunnable
 * Created by Administrator on 2015/2/1.
 */
public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {


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
    public void execute(Runnable command) {

        if (!(command instanceof LinkingRunnable)) {
            throw new IllegalArgumentException("只能提交LinkingRunnable.");
        }

        LinkingRunnable runnable = (LinkingRunnable) command;

        // messages from the same client are handled orderly
        LinkingRunnable old = runnable.getListLinkingRunnable();
        runnable.setLastLinkingRunnable(runnable);

        if (old == null) { // No previous job
            super.execute(command);
        } else {
            if (old.next.compareAndSet(null, runnable)) {
                // successfully append to previous task
            } else {
                // previous message is handled, order is guaranteed.
                super.execute(command);
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
