package dbcache.test;


import utils.typesafe.SafeActor;
import utils.typesafe.SafeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LiangZengLe
 */
public class Test7 {
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

        private Queue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
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

        void addTask3(final Task task) {
            task.start();
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
    	
        final int count = 500000;
        
        final List<Map> maps = new ArrayList<Map>();
        for (int i = 0; i < 10; i++) {
            maps.add(new Map());
        }

        
        final CountDownLatch cdh = new CountDownLatch(10 * count);
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
        for (int k = 0; k < 10; k++) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0;i < maps.size();i++) {
                        final Map map = maps.get(i);
                        for (int j = 0; j < count / 10; j++) {
                            map.addTask2(new Task(cdh, map.holder, map));
                        }
                    }
                }
            }.start();
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
        
        
        final CountDownLatch cdh1 = new CountDownLatch(10 * count);
        
        long s1 = System.currentTimeMillis();
        for (int k = 0; k < 10; k++) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0;i < maps.size();i++) {
                        final Map map = maps.get(i);
                        for (int j = 0; j < count / 10; j++) {
                            map.addTask3(new Task(cdh1, map.holder, map));
                        }
                    }
                }
            }.start();
        }
        if (!cdh1.await(10, TimeUnit.SECONDS)) {
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
