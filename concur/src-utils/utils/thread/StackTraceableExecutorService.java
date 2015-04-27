package utils.thread;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackTraceableExecutorService extends AbstractExecutorService {
	
	private static final Logger log = LoggerFactory.getLogger(StackTraceableExecutorService.class);
 
    protected final ExecutorService targetExecutorService;
 
    public StackTraceableExecutorService(ExecutorService targetExecutorService) {
        this.targetExecutorService = targetExecutorService;
    }
 
    private Runnable wrap(final Runnable task, final Exception clientStack, final String clientThreadName) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					task.run();
				} catch (RuntimeException e) {
					log.error("task execute error ", e);
					log.error("cause by submit by thread " + clientThreadName + ":", clientStack);
					throw e;
				}
			}
		};
    }
    
 
    private <T> Callable<T> wrap(final Callable<T> task, final Exception clientStack, final String clientThreadName) {
		return new Callable<T>() {
			@Override
			public T call() throws Exception {
				try {
					return task.call();
				} catch (Exception e) {
					log.error("task execute error ", e);
					log.error("cause by submit by thread " + clientThreadName + ":", clientStack);
					throw e;
				}
			}
		};
    }
 
    private Exception clientTrace() {
        return new Exception("Client stack trace");
    }
 
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask<T>(wrap(runnable, clientTrace(), Thread.currentThread().getName()), value);
    }
    
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(wrap(callable, clientTrace(), Thread.currentThread().getName()));
    }

	@Override
	public void shutdown() {
		targetExecutorService.shutdown();		
	}

	@Override
	public List<Runnable> shutdownNow() {
		return targetExecutorService.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return targetExecutorService.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return targetExecutorService.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return targetExecutorService.awaitTermination(timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		targetExecutorService.execute(wrap(command, clientTrace(), Thread.currentThread().getName()));
	}
    
}