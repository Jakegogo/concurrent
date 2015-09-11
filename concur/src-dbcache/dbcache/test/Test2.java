package dbcache.test;

import utils.typesafe.SafeActor;
import utils.typesafe.SafeType;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangZengLe
 */
public class Test2 {
    static ExecutorService service = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>()){
    	protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            printException(r, t);
        }
    };

    static class Map extends SafeType {

        void addTask(final Task task) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    task.start();
                }
            });
        }
    }
    
    private static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone())
                    future.get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null)
            t.printStackTrace();
    }


    public static void main(String[] args) {
        Map m = new Map();
        
        for (int i =0 ; i < 50000;i++) {
	        m.addTask(new Task(m));
	        m.addTask(new Task(m));
	        m.addTask(new Task(m));
        }

        service.shutdown();
        try {
            service.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Task.holder.getValue());
    }

    public static class Task extends SafeActor {
        static IntegerHolder holder = new IntegerHolder();

        public Task(SafeType safeType) {
            super(safeType);
        }

        @Override
        public void run() {
            int value = holder.getValue();
            Thread.yield();
            int newValue = value + 1;
            holder.setValue(newValue);
        }

		@Override
		public void onException(Throwable t) {
			System.err.println(t.getMessage());
		}
    }

    public static class IntegerHolder {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
