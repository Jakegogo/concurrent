package dbcache.utils.executors;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 链式执行的Runnable
 * @author Jake
 */
public abstract class SimpleLinkingRunnable implements Runnable {

    private final Runnable impl;

    AtomicReference<SimpleLinkingRunnable> next = new AtomicReference<SimpleLinkingRunnable>(null);


    public SimpleLinkingRunnable() {
        impl = null;
    }

    public SimpleLinkingRunnable(Runnable r) {
        this.impl = r;
    }

    /**
     * 返回上一次执行的SimpleLinkingRunnable
     * @return
     */
    public abstract AtomicReference<SimpleLinkingRunnable> getLastSimpleLinkingRunnable();
    
    /**
     * 执行异常回调()
     * @param t Throwable
     */
    public void onException(Throwable t) {}

    /**
     * 覆盖(重写)次方法需要在方法末尾调用super.runNext();
     */
    public void run() {
        if (this.impl == null) {
            throw new IllegalArgumentException("请传参SimpleLinkingRunnable(Runnable r),或者覆盖SimpleLinkingRunnable.run()方法.");
        }
        this.impl.run();
    }

//    /**
//     * 执行下一个任务
//     */
//    @Deprecated
//    protected void runNext() {
//        if (!next.compareAndSet(null, this)) { // has more job to run
//        	SimpleLinkingRunnable nextRunnable = next.get();
//        	if (nextRunnable != this) {
//        		nextRunnable.run();
//        	}
//        }
//    }

}