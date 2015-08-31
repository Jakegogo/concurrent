package lock.checkdeadlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by Administrator on 2014/11/19.
 */
public class AlternateDeadlockDetectingLockTest {

    //
    // Testing routines here
    //
    // These are very simple tests -- more tests will have to be written
    private static final Lock a = new AlternateDeadlockDetectingLock(false, true);

    private static final Lock b = new AlternateDeadlockDetectingLock(false, true);

    private static final Lock c = new AlternateDeadlockDetectingLock(false, true);

    private static Condition wa = a.newCondition();

    private static final Condition wb = b.newCondition();

    private static Condition wc = c.newCondition();

    private static void delaySeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
        }
    }

    private static void awaitSeconds(Condition c, int seconds) {
        try {
            c.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
    }

    private static void testOne() {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread one grab a");
                a.lock();
                delaySeconds(2);
                System.out.println("thread one grab b");
                b.lock();
                delaySeconds(2);
                a.unlock();
                b.unlock();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread two grab b");
                b.lock();
                delaySeconds(2);
                System.out.println("thread two grab a");
                a.lock();
                delaySeconds(2);
                a.unlock();
                b.unlock();
            }
        }).start();
    }

    private static void testTwo() {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread one grab a");
                a.lock();
                delaySeconds(2);
                System.out.println("thread one grab b");
                b.lock();
                delaySeconds(10);
                a.unlock();
                b.unlock();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread two grab b");
                b.lock();
                delaySeconds(2);
                System.out.println("thread two grab c");
                c.lock();
                delaySeconds(10);
                b.unlock();
                c.unlock();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread three grab c");
                c.lock();
                delaySeconds(4);
                System.out.println("thread three grab a");
                a.lock();
                delaySeconds(10);
                c.unlock();
                a.unlock();
            }
        }).start();
    }

    private static void testThree() {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread one grab b");
                b.lock();
                System.out.println("thread one grab a");
                a.lock();
                delaySeconds(2);
                System.out.println("thread one waits on b");
                awaitSeconds(wb, 10);
                a.unlock();
                b.unlock();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                delaySeconds(1);
                System.out.println("thread two grab b");
                b.lock();
                System.out.println("thread two grab a");
                a.lock();
                delaySeconds(10);
                b.unlock();
                c.unlock();
            }
        }).start();

    }

    public static void main(String args[]) {
        int test = 1;
        if (args.length > 0)
            test = Integer.parseInt(args[0]);
        switch (test) {
            case 1:
                testOne(); // 2 threads deadlocking on grabbing 2 locks
                break;
            case 2:
                testTwo(); // 3 threads deadlocking on grabbing 2 out of 3 locks
                break;
            case 3:
                testThree(); // 2 threads deadlocking on 2 locks with CV wait
                break;
            default:
                System.err.println("usage: java DeadlockDetectingLock [ test# ]");
        }
        delaySeconds(60);
        System.out.println("--- End Program ---");
        System.exit(0);
    }


}
