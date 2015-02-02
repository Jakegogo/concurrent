package dbcache.utils.concurrent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 链式执行的Runnable
 * @author Jake
 */
public abstract class LinkingRunnable implements Runnable {

    private final Runnable impl;

    AtomicReference<LinkingRunnable> next = new AtomicReference<LinkingRunnable>(null);


    public LinkingRunnable() {
        impl = null;
    }

    public LinkingRunnable(Runnable r) {
        this.impl = r;
    }

    /**
     * 返回上一次执行的LinkingRunnable
     * @return
     */
    public abstract AtomicReference<LinkingRunnable> getLastLinkingRunnable();

    /**
     * 覆盖(重写)次方法需要在方法末尾调用super.runNext();
     */
    public void run() {
        if (this.impl == null) {
            throw new IllegalArgumentException("请传参LinkingRunnable(Runnable r),或者覆盖LinkingRunnable.run()方法.");
        }
        this.impl.run();
        this.runNext();
    }

    protected void runNext() {
        if (!next.compareAndSet(null, this)) { // has more job to run
            next.get().run();
        }
    }

}