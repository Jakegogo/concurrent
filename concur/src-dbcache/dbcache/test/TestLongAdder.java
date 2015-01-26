package dbcache.test;

import dbcache.utils.concurrent.LongAdder;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2015/1/23.
 */
public class TestLongAdder {

    public static void main(String[] args) {

        long t1 = System.currentTimeMillis();
        AtomicLong acl = new AtomicLong(1);
        for(int i = 0;i < 100000000;i++) {
            acl.incrementAndGet();
        }
        System.out.println(System.currentTimeMillis() - t1);



        t1 = System.currentTimeMillis();
        LongAdder adder = new LongAdder();
        for(int i = 0;i < 100000000;i++) {
            adder.increment();
            adder.intValue();
        }
        System.out.println(System.currentTimeMillis() - t1);

    }

}
