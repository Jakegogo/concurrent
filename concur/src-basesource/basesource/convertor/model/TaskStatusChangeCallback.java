package basesource.convertor.model;

/**
 * 任务状态改变回调接口
 * Created by Jake on 2015/6/14.
 */
public interface TaskStatusChangeCallback {

    /**
     * 任务开始回调
     */
    public void onStart();

    /**
     * 任务完成回调方法
     */
    public void onComplete();

}
