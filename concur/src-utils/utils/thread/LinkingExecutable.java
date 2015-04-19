package utils.thread;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 可链式执行的任务
 * Created by Jake on 2015/2/3.
 */
public interface LinkingExecutable {

    /**
     * 返回上一次执行的LinkingRunnable
     * @return
     */
    public AtomicReference<LinkingExecutable> getLastLinkingRunnable();

    /**
     * 获取下一个任务
     * @return
     */
    public LinkingRunnableFutureTask fetchNext();

    /**
     * LinkingExecutable.next
     * @return
     */
    public AtomicReference<LinkingRunnableFutureTask> getNext();

}
