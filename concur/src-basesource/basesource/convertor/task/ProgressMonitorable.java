package basesource.convertor.task;

/**
 * 带进度显示的
 * Created by Jake on 2015/6/14.
 */
public interface ProgressMonitorable {

    /**
     * 更新进度显示
     * @param progress
     */
    public void updateProgress(double progress);

}
