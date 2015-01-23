package dbcache.test;

import dbcache.utils.concurrent.LongAdder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2015/1/23.
 */
public class TestLongAdder1 {

    public static void main(String[] args) {

        final AtomicLong acl = new AtomicLong(0);
        final LongAdder adder = new LongAdder();

        final CountDownLatch ct1 = new CountDownLatch(1);
        final CountDownLatch ct2 = new CountDownLatch(8);

        for(int t = 0;t < 8;t++) {
            new Thread() {
                public void run() {
                    try {
                        ct1.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for(int i = 0;i < 1000000;i++) {
                        acl.incrementAndGet();
                    }

                    ct2.countDown();

                }
            }.start();
        }

        long t1 = System.currentTimeMillis();
        ct1.countDown();
        try {
            ct2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - t1);


        final CountDownLatch ct3 = new CountDownLatch(1);
        final CountDownLatch ct4 = new CountDownLatch(8);

        for(int t = 0;t < 8;t++) {
            new Thread() {
                public void run() {
                    try {
                        ct3.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for(int i = 0;i < 1000000;i++) {
                        adder.increment();
                        adder.intValue();
                    }


                    ct4.countDown();

                }
            }.start();
        }

        t1 = System.currentTimeMillis();
        ct3.countDown();
        try {
            ct4.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - t1);


        System.out.println(acl.get());
        System.out.println(adder.intValue());

//        long t1 = System.currentTimeMillis();
//
//
//        System.out.println(System.currentTimeMillis() - t1);
//
//
//
//        t1 = System.currentTimeMillis();
//
//
//        System.out.println(System.currentTimeMillis() - t1);

    }

}
