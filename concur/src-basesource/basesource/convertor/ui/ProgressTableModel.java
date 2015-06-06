package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件列表表格模型
 * A TableModel to hold File[].
 */
public class ProgressTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 9114477369652282804L;

    private Map<Integer, Double> progresses = new HashMap<Integer, Double>();
	
	private File[] files;
	/** FileSystemView */
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    ProgressTableModel() {
        this(new File[0]);
    }

    ProgressTableModel(File[] files) {
        this.files = files;
    }

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

    public int getRowCount() {
        return files.length;
    }

    public File getFile(int row) {
        return files[row];
    }

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

    public double getProgress(int row) {
        Double progress = progresses.get(row);
        if (progress == null) {
            progress = Math.random();
            progresses.put(row, progress);
        }
        return progress;
    }
}