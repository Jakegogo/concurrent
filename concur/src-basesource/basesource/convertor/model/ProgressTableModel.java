package basesource.convertor.model;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.ui.extended.RowProgressTableUI;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.*;

/**
 * 文件列表表格模型
 * A TableModel to hold File[].
 */
public class ProgressTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 9114477369652282804L;

    private Map<Integer, Double> progresses = new HashMap<Integer, Double>();
	
	private File[] files;
	
	private JTable jTable;
	
	/** FileSystemView */
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public ProgressTableModel(JTable jTable) {
        this(new File[0], jTable);
    }

    public ProgressTableModel(File[] files, JTable jTable) {
        this.files = files;
        this.jTable = jTable;
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
    	progresses.put(row, progress);
    	RowProgressTableUI.updateProgressUI(jTable, row, row);
    }

    /**
     * 重置进度
     */
    public void clearProgress() {
    	progresses.clear();
        RowProgressTableUI.clearProgressUI(jTable);
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