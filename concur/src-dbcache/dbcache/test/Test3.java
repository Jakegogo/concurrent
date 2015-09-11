package dbcache.test;

import utils.typesafe.SafeActor;
import utils.typesafe.SafeType;
import utils.typesafe.extended.MultiSafeActor;
import utils.typesafe.extended.MultiSafeType;

import java.util.concurrent.ArrayBlockingQueue;
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
public class Test3 {
    static ExecutorService service = new ThreadPoolExecutor(4, 4, 1, TimeUnit.MINUTES, //
    		new LinkedBlockingQueue<Runnable>(),//
            Executors.defaultThreadFactory()) {

        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            printException(r, t);
        }
    };

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
    
    static class Map extends MultiSafeType {

        void addTask(final Task task) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    task.start();
                }
            });
        }
    }


    public static void main(String[] args) {
        Map m = new Map();
        
        for (int i =0 ; i < 3000;i++) {
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

    public static class Task extends MultiSafeActor {
        static IntegerHolder holder = new IntegerHolder();

        public Task(MultiSafeType safeType) {
            super(service, safeType);
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
			t.printStackTrace();
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
