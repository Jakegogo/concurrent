package dbcache.utils.executor;

import dbcache.utils.NamedThreadFactory;
import dbcache.utils.executor.ThreadPoolExecutor.Worker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 顺序执行的线程池
 * <br/>顺序执行;同时复用线程,减少线程切换。适用于并发提交
 * <br/> 只能提交LinkingRunnable
 * Created by Jake on 2015/2/1.
 */
public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {

	protected final HashSet<Worker> workers = new HashSet<Worker>();
	
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
        	assert task instanceof LinkingRunnable;
        	
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
                try {
                    task.run();
                    
                    LinkingRunnable next = (LinkingRunnable) task;
                    while ((next = fetchNext(next)) != null) {
                    	next.run();
                    }
                    
                    ran = true;
                    afterExecute(task, null);
                    ++completedTasks;
                } catch (RuntimeException ex) {
                    if (!ran)
                        afterExecute(task, ex);
                    throw ex;
                }
            } finally {
                runLock.unlock();
            }
        }

        /**
         * 获取下一个任务
         * @param linkingRunnable
         * @return
         */
        LinkingRunnable fetchNext(LinkingRunnable linkingRunnable) {
        	if (!linkingRunnable.next.compareAndSet(null, linkingRunnable)) {
        		return linkingRunnable.next.get();
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
