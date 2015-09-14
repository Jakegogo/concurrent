package dbcache.test;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;

import dbcache.test.Test7.IntegerHolder;
import dbcache.test.Test7.Task;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LiangZengLe
 */
public class Test8 {
    private Disruptor<Event> disruptor;

    
    final CountDownLatch cdh1 = new CountDownLatch(10 * count);
    
    private IntegerHolder holder = new IntegerHolder();
    
    static AtomicInteger counter = new AtomicInteger();
    
    static int count = 500000;
    
    @Before
    public void setup() {
        Disruptor<Event> disruptor = new Disruptor<Event>(new EventFactory<Event>() {
    		public Event newInstance() {
    			return new Event();
    		}
    	}, 1024 * 8, Executors.newFixedThreadPool(5));
        
        disruptor.handleEventsWith(
        		new EventHandler<Event>() {
					@Override
					public void onEvent(Event event, long sequence,
							boolean endOfBatch) throws Exception {
						cdh1.countDown();
						
						int value = holder.getValue();
			            Thread.yield();
			            int newValue = value + 1;
			            holder.setValue(newValue);

			            counter.incrementAndGet();
					}
        		});
        
        disruptor.start();
        this.disruptor = disruptor;

    }

    @org.junit.Test
    public void test() {
    	
    	long t1 =System.currentTimeMillis();
    	
    	for (int i = 0;i < 10;i++) {
	        new Thread() {
	            @Override
	            public void run() {
	                for (int j = 0; j < count; j++) {
	                    disruptor.getRingBuffer().publishEvent(new EventTranslatorOneArg<Event, Integer>() {
	
							@Override
							public void translateTo(Event event,
									long sequence, Integer arg0) {
								event.setValue(arg0);
							}
	                    	
	                    }, j);
	                }
	            }
	        }.start();
    	}
    	
    	try {
			cdh1.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	System.out.println("count " + counter.get());
    	
    	System.out.println(System.currentTimeMillis() -  t1);
    }

    @After
    public void tearDown() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
    }

    public static class Event {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
