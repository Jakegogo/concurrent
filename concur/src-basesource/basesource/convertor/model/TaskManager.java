package basesource.convertor.model;

import basesource.convertor.task.ConvertTask;
import utils.thread.NamedThreadFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务管理器
 * Created by Jake on 2015/6/13.
 */
public class TaskManager {

    /** 任务执行线程输 */
    private static final int taskThreadSize = 2;

    /**
     * 任务执行线程池
     */
    private ExecutorService DB_POOL_SERVICE;

    /**
     * 当前任务
     */
    private ConvertTask curTask;

    /**
     * 文件表格数据模型
     */
    private ProgressTableModel tableModel;


    public TaskManager(ProgressTableModel tableModel) {
        this.tableModel = tableModel;
        this.initExcutor();
    }

    private void initExcutor() {
        // 初始化线程池
        ThreadGroup threadGroup = new ThreadGroup("基础数据表转换模块");
        NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "转换任务线程池");

        DB_POOL_SERVICE = Executors.newFixedThreadPool(taskThreadSize, threadFactory);
    }


    /**
     * 开始任务
     */
    public void start(TaskCompleteCallback completeCallback) {
        DB_POOL_SERVICE.submit(createRunnable(completeCallback));
    }


    /**
     * 创建任务Runnable
     * @param completeCallback
     * @return
     */
    private Runnable createRunnable(final TaskCompleteCallback completeCallback) {
        return new Runnable() {
            @Override
            public void run() {
                // 不存在则创建新的任务
                if (curTask == null) {
                    File inputPath = UserConfig.getInstance().getInputPath();
                    curTask = new ConvertTask(inputPath, tableModel);
                    curTask.start();
                } else {
                    curTask.start();
                }

                if (!curTask.isStop()) {
                    // 任务结束回调
                    completeCallback.onComplete();
                }
            }
        };
    }


    /**
     * 暂停当前任务
     * @return
     */
    public boolean stop() {
        if (curTask == null) {
            return false;
        }
        curTask.stop();
        return true;
    }


    /**
     * 取消当前任务
     */
    public boolean cancel() {
        if (curTask == null) {
            return false;
        }
        curTask.reset();
        return true;
    }

    /**
     * 改变输入路径
     * @param path
     */
    public void changeInputPath(File path) {
        if (curTask != null) {
            curTask.changeInputPath(path);
        }
    }


}
