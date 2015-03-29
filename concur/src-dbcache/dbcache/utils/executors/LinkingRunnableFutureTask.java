package dbcache.utils.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 链式执行的FutureTask
 * Created by Jake on 2015/2/3.
 */
public class LinkingRunnableFutureTask<V> extends FutureTask<V> {

    /**
     * PLACE_HOLDER
     */
    public static final LinkingRunnableFutureTask PLACE_HOLDER = new LinkingRunnableFutureTask(new LinkingRunnable() {
        @Override
        public AtomicReference<LinkingExecutable> getLastLinkingRunnable() {
            return null;
        }
    }, null);


    private LinkingExecutable linkingExecutable;


    public LinkingRunnableFutureTask(Callable<V> callable) {
        super(callable);
        if (!(callable instanceof LinkingExecutable)) {
            throw new IllegalArgumentException("只能提交LinkingCallable.");
        }
        this.linkingExecutable = (LinkingExecutable) callable;
    }


    public LinkingRunnableFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        if (!(runnable instanceof LinkingExecutable)) {
            throw new IllegalArgumentException("只能提交LinkingRunnable.");
        }
        this.linkingExecutable = (LinkingExecutable) runnable;
    }

    /**
     * 获取当前任务
     * @return
     */
    public LinkingExecutable getLinkingExecutable() {
        return this.linkingExecutable;
    }

    /**
     * 获取下一个任务
     * @return
     */
    public LinkingRunnableFutureTask fetchNext() {
        return this.linkingExecutable.fetchNext();
    }

}
