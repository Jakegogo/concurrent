package dbcache.test;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import utils.typesafe.SafeActor;
import utils.typesafe.SafeType;

/**
 * @author LiangZengLe
 */
public class Test4 {
    static ExecutorService service = Executors.newFixedThreadPool(8);

    static class Map extends SafeType {
        IntegerHolder holder = new IntegerHolder();

        void addTask(final Task task) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    task.start();
                }
            });
        }

        private Queue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();
        private AtomicBoolean running = new AtomicBoolean();

        void addTask2(final Runnable runnable) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } finally {
                        running.compareAndSet(true, false);
                    }
                }
            });
        }

        Runnable nextTask() {
            if (!running.compareAndSet(false, true)) {
                return null;
            }
            Runnable poll = tasks.poll();
            if (poll == null) {
                running.set(false);
            }
            return poll;
        }
    }

    public static class Task extends SafeActor {
        static AtomicInteger counter = new AtomicInteger();
        private CountDownLatch cdh;
        private IntegerHolder holder;

        public Task(CountDownLatch cdh, IntegerHolder holder, SafeType safeType) {
        	super(safeType);
            this.cdh = cdh;
            this.holder = holder;
        }

        @Override
        public void run() {
            int value = holder.getValue();
            Thread.yield();
            int newValue = value + 1;
            holder.setValue(newValue);

            this.cdh.countDown();
            counter.incrementAndGet();
        }
    }

    volatile static boolean stop = false;

    public static void main(String[] args) throws InterruptedException {
    	
//    	Thread.sleep(10000);
//    	System.out.println("start");
    	
        int count = 500000;
        
        final List<Map> maps = new ArrayList<Map>();
        for (int i = 0; i < 10; i++) {
            maps.add(new Map());
        }

        
        CountDownLatch cdh = new CountDownLatch(10 * count);
        for (int i = 0; i < 4; i++) {
            service.submit(new Runnable() {
            	public void run() {
	                Iterator<Map> it = maps.iterator();
	                boolean sleep = true;
	                while (it.hasNext()) {
	                    Map map = it.next();
	                    Runnable task = map.nextTask();
	                    if (task != null) {
	                        task.run();
	                        sleep = false;
	                    }
	
	                    if (it.hasNext()) {
	                        continue;
	                    }
	                    it = maps.iterator();
	                    if (sleep) {
	                        try {
	                            TimeUnit.MILLISECONDS.sleep(5);
	                        } catch (InterruptedException e) {
	
	                        }
	                    }
	                    sleep = true;
	                    if (stop) {
	                        break;
	                    }
	                }
            	}
            });
        }
        long s2 = System.currentTimeMillis();
        for (Map map : maps) {
            for (int j = 0; j < count; j++) {
                map.addTask2(new Task(cdh, map.holder, map));
            }
        }
        cdh.await(10, TimeUnit.SECONDS);
        System.out.println("queue: " + (System.currentTimeMillis() - s2));
        for (Map map : maps) {
            if (map.holder.getValue() != count) {
                System.err.println("queue 结果错误");
            }
        }
        

        for (Map map : maps) {
            map.holder.setValue(0);
        }
        
        
        cdh = new CountDownLatch(10 * count);
        
        long s1 = System.currentTimeMillis();
        for (Map map : maps) {
            for (int j = 0; j < count; j++) {
                map.addTask(new Task(cdh, map.holder, map));
            }
        }
        if (!cdh.await(10, TimeUnit.SECONDS)) {
            System.err.println("actor 执行超时");
        }
        System.out.println("safe actor: " + (System.currentTimeMillis() - s1));
        for (Map map : maps) {
            if (map.holder.getValue() != count) {
                System.err.println("actor 结果错误");
            }
        }

        
        System.out.println("count " + Task.counter.get());
        stop = true;
        
//        Thread.sleep(100000);
        
        service.shutdown();
        service.awaitTermination(10, TimeUnit.SECONDS);
        service.shutdownNow();
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
