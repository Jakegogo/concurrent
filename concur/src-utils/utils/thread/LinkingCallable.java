package utils.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 链式执行的Runnable
 * @author Jake
 */
public abstract class LinkingCallable<V> implements Callable<V>, LinkingExecutable {

    private final Callable<V> impl;

    @SuppressWarnings("rawtypes")
	AtomicReference<LinkingRunnableFutureTask> next = new AtomicReference<LinkingRunnableFutureTask>(null);


    public LinkingCallable() {
        impl = null;
    }

    public LinkingCallable(Callable<V> c) {
        this.impl = c;
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
     * 覆盖(重写)次方法需要在方法末尾调用super.executeNext();
     */
    public V call() throws Exception {
        if (this.impl == null) {
            throw new IllegalArgumentException("请传参LinkingCallable(Callable<V> c),LinkingCallable.call()方法.");
        }
        return this.impl.call();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public LinkingRunnableFutureTask fetchNext() {
        if (!next.compareAndSet(null, LinkingRunnableFutureTask.PLACE_HOLDER)) {
            return next.get();
        }
        return null;
    }

}