package basesource.convertor.model;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.task.TaskInfo;
import basesource.convertor.task.TaskStatus;
import basesource.convertor.ui.extended.RowProgressTableUI;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

/**
 * 文件列表表格模型
 * A TableModel to hold File[].
 */
public class ProgressTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 9114477369652282804L;

    private Map<Integer, Double> progresses = new HashMap<Integer, Double>();
	
	private File[] files;

    /** 任务信息 */
    private TaskInfo[] taskInfo;
	
	private JTable jTable;

    /** 父容器 必须为滚动面板 */
    private JScrollPane parent;
	
	/** FileSystemView */
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public ProgressTableModel(JTable jTable, JScrollPane parent) {
        this(new File[0], jTable, parent);
    }

    public ProgressTableModel(File[] files, JTable jTable, JScrollPane parent) {
        this.files = files;
        this.jTable = jTable;
        this.parent = parent;
        this.taskInfo = new TaskInfo[files.length];
    }


    /**
     * 获取含排序信息的文件列表
     * @return
     */
    public List<File> getSortedRowFiles() {
        int size = this.files.length;
        List<File> rows = new ArrayList<File>(size);
        for (int i = 0;i < size;i++) {
            File f = this.files[this.jTable.convertRowIndexToModel(i)];
            rows.add(f);
        }
        return rows;
    }

    /**
     * 获取总行数
     * @return
     */
    public int getRowCount() {
        return files.length;
    }

    /**
     * 获取行文件
     * @param row 行号 从0开始
     * @return
     */
    public File getFile(int row) {
    	if (row < 0 || row >= this.files.length) {
    		return null;
    	}
        return files[row];
    }

    /**
     * 设置模型的文件值
     * @param files File[]
     */
    public void setFiles(File[] files) {
        this.files = files;
        this.taskInfo = new TaskInfo[files.length];
        fireTableDataChanged();
    }

    public boolean isSameFiles(File[] files) {
        if (files == null) {
            return false;
        }
        return files.equals(this.files);
    }

    /**
     * 获取行进度
     * @param row 行号 从0开始
     * @return
     */
    public double getProgress(int row) {
    	if (row < 0 || row >= this.files.length) {
    		return 0d;
    	}
        Double progress = progresses.get(row);
        if (progress == null) {
            return 0d;
        }
        return progress;
    }

    /**
     * 改变进度
     * @param row 行号 从0开始
     * @param progress 进度 double 最大值1d
     */
    public void changeProgress(int row, double progress) {
    	if (row < 0 || row >= this.files.length) {
    		return;
    	}
        Double oldProgress = progresses.get(row);
        if (oldProgress != null && oldProgress < 0) {
            return;
        }
    	progresses.put(row, progress);
    	RowProgressTableUI.updateProgressUI(jTable, row, row);

        checkScroll(row);

    }

    /**
     * 标记为任务失败
     * @param row 行号 从0开始
     */
    public void markAsFail(int row, String name, Exception e) {
        if (row < 0 || row >= this.files.length) {
            return;
        }
        // 保存失败信息
        this.saveTaskInfo(row, name, e);

        Double progress = progresses.get(row);
        if (progress != null && progress < 0) {
            return;
        }
        if (progress == null) {
            progress = -1d;
        } else {
            progress = - progress;
        }
        progresses.put(row, progress);
        RowProgressTableUI.updateProgressUI(jTable, row, row);

        checkScroll(row);
    }

    // 保存任务信息
    private void saveTaskInfo(int row, String name, Exception e) {
        if (row < 0 || row >= this.files.length) {
            return;
        }

        TaskInfo taskInfo = this.taskInfo[row];
        if (taskInfo == null) {
            taskInfo = new TaskInfo();
            this.taskInfo[row] = taskInfo;
        }

        taskInfo.setTaskStatus(TaskStatus.EXCEPTION);
        taskInfo.getFailSheets().add(name);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String errorInfo = sw.toString();
        errorInfo = errorInfo.replace("\n", "<br/>&nbsp;&nbsp;");
        taskInfo.getFailErrors().add(errorInfo);
    }


    /**
     * 获取任务信息
     * @param row 行号 从0开始
     * @return
     */
    public TaskInfo getTaskInfo(int row) {
        if (row < 0 || row >= this.files.length) {
            return null;
        }

        TaskInfo taskInfo = this.taskInfo[row];
        return taskInfo;
    }


    /**
     * 判断任务是否失败
     * @param row 行号 从0开始
     * @return
     */
    public boolean isFail(int row) {
        if (row < 0 || row >= this.files.length) {
            return false;
        }

        Double progress = progresses.get(row);
        if (progress != null && progress < 0) {
            return true;
        }
        return false;
    }

    /**
     * 重置进度
     */
    public void clearProgress() {
    	progresses.clear();
        RowProgressTableUI.clearProgressUI(jTable);
    }


    // 滚动到对应位置
    private void checkScroll(int row) {
        Rectangle cellRectangle = jTable.getCellRect(jTable.convertRowIndexToView(row), 0, true);
        // 获取JScrollPane中的纵向JScrollBar
        JScrollBar sBar = this.parent.getVerticalScrollBar();

        if (cellRectangle.y > sBar.getVisibleAmount()) {
            sBar.setValue(cellRectangle.y);
        }

        if (row == 0) {
            sBar.setValue(0);
        }
    }

    // --- for JTable ---

    public Object getValueAt(int row, int column) {
        File file = files[row];
        switch (column) {
            case 0:
                return this.getFileIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
                return file.length();
            case 3:
                return file.lastModified();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    private Object getFileIcon(File file) {
        Icon icon = fileSystemView.getSystemIcon(file);
        return icon;
    }


    public int getColumnCount() {
        return DefaultUIConstant.FILE_TABLE_HREADER.length;
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return Icon.class;
            case 2:
                return Long.class;
            case 3:
                return Date.class;
        }
        return String.class;
    }

    public String getColumnName(int column) {
        return DefaultUIConstant.FILE_TABLE_HREADER[column];
    }


}