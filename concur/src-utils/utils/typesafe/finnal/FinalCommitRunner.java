package utils.typesafe.finnal;

import utils.JsonUtils;
import utils.typesafe.SafeActor;
import utils.typesafe.SafeRunner;
import utils.typesafe.SafeType;

/**
 * 线程安全的FinalCommitRunner
 * Created by Jake on 10/1 0001.
 */
public class FinalCommitRunner extends SafeRunner {
    /**
     * 构造方法
     *
     * @param safeType  当前对象
     * @param safeActor 当前任务
     */
    protected FinalCommitRunner(SafeType safeType, SafeActor safeActor) {
        super(safeType, safeActor);
    }

    /**
     * 执行队列
     */
    protected void runNext() {
        FinalCommitRunner current = this;
        do {
            if (current.next == null) {
                try {
                    current.safeActor.run();
                } catch (Exception e) {
                    current.safeActor.onException(e);
                }
            }
        } while ((current = current.next()) != null);// 获取下一个任务
    }

    /**
     * 下一个任务
     */
    protected FinalCommitRunner next() {
        if (!UNSAFE.compareAndSwapObject(this, nextOffset, null, this)) { // has more job to run
            return (FinalCommitRunner) next;
        }
        return null;
    }

}
