package basesource.convertor.task;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务信息
 * Created by Jake on 2015/6/15.
 */
public class TaskInfo {

    /**
     * 任务状态
     */
    private TaskStatus taskStatus;

    /**
     * 失败的表格名称
     */
    private List<String> failSheets = new ArrayList<String>();

    /**
     * 失败的异常信息
     */
    private List<String> failErrors = new ArrayList<String>();


    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<String> getFailSheets() {
        return failSheets;
    }

    public void setFailSheets(List<String> failSheets) {
        this.failSheets = failSheets;
    }

    public List<String> getFailErrors() {
        return failErrors;
    }

    public void setFailErrors(List<String> failErrors) {
        this.failErrors = failErrors;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        for (int i = 0;i < failSheets.size();i++) {
            sb.append("<B>");
            sb.append(failSheets.get(i));
            sb.append("</B>");
            sb.append("表转换失败:");
            sb.append(failErrors.get(i));
            sb.append("<br/>");
        }
        sb.append("</html>");
        return sb.toString();
    }
}
