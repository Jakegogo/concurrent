package dbcache.utils.executors;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 链式执行的Runnable
 * @author Jake
 */
public abstract class LinkingRunnable implements Runnable, LinkingExecutable {

    private final Runnable impl;

    AtomicReference<LinkingRunnableFutureTask> next = new AtomicReference<LinkingRunnableFutureTask>(null);


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
    public abstract AtomicReference<LinkingExecutable> getLastLinkingRunnable();
    
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
            throw new IllegalArgumentException("请传参LinkingRunnable(Runnable r),或者覆盖LinkingRunnable.run()方法.");
        }
        this.impl.run();
    }

    @Override
    public LinkingRunnableFutureTask fetchNext() {
        if (!next.compareAndSet(null, LinkingRunnableFutureTask.PLACE_HOLDER)) {
            return next.get();
        }
        return null;
    }

    @Override
    public AtomicReference<LinkingRunnableFutureTask> getNext() {
        return next;
    }
}