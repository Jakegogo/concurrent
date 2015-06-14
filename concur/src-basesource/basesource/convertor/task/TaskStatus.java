package basesource.convertor.task;

/**
 * 任务状态枚举
 */
public enum TaskStatus {

    /**
     * 初始化状态
     */
    INIT,

    /**
     * 任务进行中
     */
    STARTED,

    /**
     * 已经暂停
     */
    STOPED,

    /**
     * 已经完成
     */
    FINISHED,

    /**
     * 已经取消
     */
    CANCEL

}
