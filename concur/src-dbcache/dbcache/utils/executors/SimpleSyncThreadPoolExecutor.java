package dbcache.utils.executors;

import dbcache.utils.NamedThreadFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 顺序执行的线程池
 * <br/>顺序执行;同时复用线程,减少线程切换。适用于并发提交
 * <br/> 只能提交SimpleLinkingRunnable
 * Created by Jake on 2015/2/1.
 */
public class SimpleSyncThreadPoolExecutor extends ThreadPoolExecutor {

	protected final HashSet<Worker> workers = new HashSet<Worker>();


    public SimpleSyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public SimpleSyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public SimpleSyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public SimpleSyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public Future<?> submit(Runnable task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Runnable command) {

        if (!(command instanceof SimpleLinkingRunnable)) {
            throw new IllegalArgumentException("只能提交SimpleLinkingRunnable.");
        }

        SimpleLinkingRunnable runnable = (SimpleLinkingRunnable) command;
		appendSubmit(runnable);
	}

	private void appendSubmit(SimpleLinkingRunnable runnable) {

        // messages from the same client are handled orderly
        AtomicReference<SimpleLinkingRunnable> lastRef = runnable.getLastSimpleLinkingRunnable();

        SimpleLinkingRunnable last = lastRef.get();
        lastRef.set(runnable);

        if (last == null) { // No previous job
            super.execute(runnable);
        } else {
            if (last.next.compareAndSet(null, runnable)) {
                // successfully append to previous task
            } else {
                // previous message is handled, order is guaranteed.
                super.execute(runnable);
            }
        }

	}


	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		if (t != null) {
			SimpleLinkingRunnable runnable = (SimpleLinkingRunnable) r;
			runnable.onException(t);
		}
	}

	
	@Override
    protected Thread addThread(Runnable firstTask) {
        Worker w = new Worker(firstTask);
        Thread t = threadFactory.newThread(w);
        if (t != null) {
            w.thread = t;
            workers.add(w);
            int nt = ++poolSize;
            if (nt > largestPoolSize)
                largestPoolSize = nt;
        }
        return t;
    }
	
	
	@Override
	public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }
	
	
	@Override
	public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers) {
                if (w.isActive())
                    ++n;
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }
	
	@Override
	public long getTaskCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			long n = completedTaskCount;
			for (Worker w : workers) {
				n += w.completedTasks;
				if (w.isActive())
					++n;
			}
			return n + workQueue.size();
		} finally {
			mainLock.unlock();
		}
	}
	
	@Override
	public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0)
            throw new IllegalArgumentException();
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int extra = this.corePoolSize - corePoolSize;
            this.corePoolSize = corePoolSize;
            if (extra < 0) {
                int n = workQueue.size(); // don't add more threads than tasks
                while (extra++ < 0 && n-- > 0 && poolSize < corePoolSize) {
                    Thread t = addThread(null);
                    if (t != null)
                        t.start();
                    else
                        break;
                }
            }
            else if (extra > 0 && poolSize > corePoolSize) {
                try {
                    Iterator<Worker> it = workers.iterator();
                    while (it.hasNext() &&
                           extra-- > 0 &&
                           poolSize > corePoolSize &&
                           workQueue.remainingCapacity() == 0)
                        it.next().interruptIfIdle();
                } catch (SecurityException ignore) {
                    // Not an error; it is OK if the threads stay live
                }
            }
        } finally {
            mainLock.unlock();
        }
    }
	
	@Override
	void interruptIdleWorkers() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker w : workers)
				w.interruptIfIdle();
		} finally {
			mainLock.unlock();
		}
	}
	
	@Override
	public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
            throw new IllegalArgumentException();
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int extra = this.maximumPoolSize - maximumPoolSize;
            this.maximumPoolSize = maximumPoolSize;
            if (extra > 0 && poolSize > maximumPoolSize) {
                try {
                    Iterator<Worker> it = workers.iterator();
                    while (it.hasNext() &&
                           extra > 0 &&
                           poolSize > maximumPoolSize) {
                        it.next().interruptIfIdle();
                        --extra;
                    }
                } catch (SecurityException ignore) {
                    // Not an error; it is OK if the threads stay live
                }
            }
        } finally {
            mainLock.unlock();
        }
    }
	
	@Override
	public void shutdown() {

	SecurityManager security = System.getSecurityManager();
	if (security != null)
            security.checkPermission(shutdownPerm);

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (security != null) { // Check if caller can modify our threads
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            }

            int state = runState;
            if (state < SHUTDOWN)
                runState = SHUTDOWN;

            try {
                for (Worker w : workers) {
                    w.interruptIfIdle();
                }
            } catch (SecurityException se) { // Try to back out
                runState = state;
                // tryTerminate() here would be a no-op
                throw se;
            }

            tryTerminate(); // Terminate now if pool and queue empty
        } finally {
            mainLock.unlock();
        }
    }

    
	@Override
    public List<Runnable> shutdownNow() {

	SecurityManager security = System.getSecurityManager();
	if (security != null)
            security.checkPermission(shutdownPerm);

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (security != null) { // Check if caller can modify our threads
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            }

            int state = runState;
            if (state < STOP)
                runState = STOP;

            try {
                for (Worker w : workers) {
                    w.interruptNow();
                }
            } catch (SecurityException se) { // Try to back out
                runState = state;
                // tryTerminate() here would be a no-op
                throw se;
            }

            List<Runnable> tasks = drainQueue();
            tryTerminate(); // Terminate now if pool and queue empty
            return tasks;
        } finally {
            mainLock.unlock();
        }
    }
	
	// Worker
	protected final class Worker implements Runnable {
        /**
         * The runLock is acquired and released surrounding each task
         * execution. It mainly protects against interrupts that are
         * intended to cancel the worker thread from instead
         * interrupting the task being run.
         */
        private final ReentrantLock runLock = new ReentrantLock();

        /**
         * Initial task to run before entering run loop. Possibly null.
         */
        private Runnable firstTask;

        /**
         * Per thread completed task counter; accumulated
         * into completedTaskCount upon termination.
         */
        volatile long completedTasks;

        /**
         * Thread this worker is running in.  Acts as a final field,
         * but cannot be set until thread is created.
         */
        Thread thread;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        boolean isActive() {
            return runLock.isLocked();
        }

        /**
         * Interrupts thread if not running a task.
         */
        void interruptIfIdle() {
            final ReentrantLock runLock = this.runLock;
            if (runLock.tryLock()) {
                try {
		    if (thread != Thread.currentThread())
			thread.interrupt();
                } finally {
                    runLock.unlock();
                }
            }
        }

        /**
         * Interrupts thread even if running a task.
         */
        void interruptNow() {
            thread.interrupt();
        }

        /**
         * Runs a single task between before/after methods.
         */
        private void runTask(Runnable task) {
        	assert task instanceof SimpleLinkingRunnable;
        	
            final ReentrantLock runLock = this.runLock;
            runLock.lock();
            try {
                /*
                 * Ensure that unless pool is stopping, this thread
                 * does not have its interrupt set. This requires a
                 * double-check of state in case the interrupt was
                 * cleared concurrently with a shutdownNow -- if so,
                 * the interrupt is re-enabled.
                 */
                if (runState < STOP &&
                    Thread.interrupted() &&
                    runState >= STOP)
                    thread.interrupt();
                /*
                 * Track execution state to ensure that afterExecute
                 * is called only if task completed or threw
                 * exception. Otherwise, the caught runtime exception
                 * will have been thrown by afterExecute itself, in
                 * which case we don't want to call it again.
                 */
                boolean ran = false;
                beforeExecute(thread, task);
                SimpleLinkingRunnable next = (SimpleLinkingRunnable) task;
                try {
                	
                    do {
                    	next.run();
                    	afterExecute(next, null);
                    } while ((next = fetchNext(next)) != null);
                    
                    ran = true;
                    ++completedTasks;
                } catch (RuntimeException ex) {
                    if (!ran) {
                    	do {
                    		afterExecute(next, ex);
                        } while ((next = fetchNext(next)) != null);
                    }
                    throw ex;
                }
            } finally {
                runLock.unlock();
            }
        }

        /**
         * 获取下一个任务
         * @param SimpleLinkingRunnable
         * @return
         */
        SimpleLinkingRunnable fetchNext(SimpleLinkingRunnable SimpleLinkingRunnable) {
        	if (!SimpleLinkingRunnable.next.compareAndSet(null, SimpleLinkingRunnable)) {
        		return SimpleLinkingRunnable.next.get();
        	}
        	return null;
        }
        
        /**
         * Main run loop
         */
        public void run() {
            try {
                Runnable task = firstTask;
                firstTask = null;
                while (task != null || (task = getTask()) != null) {
                    runTask(task);
                    task = null;
                }
            } finally {
                workerDone(this);
            }
        }
    }
	
	
	/**
     * Performs bookkeeping for an exiting worker thread.
     * @param w the worker
     */
    protected void workerDone(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
            if (--poolSize == 0)
                tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

	/**
	 * 创建ExecutorService
	 * 使用CallerRunsPolicy拒绝策略
	 * @param nThreads
	 * @param threadFactory
	 * @return
	 */
	public static SimpleSyncThreadPoolExecutor newFixedThreadPool(int nThreads, NamedThreadFactory threadFactory) {
        return new SimpleSyncThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
}