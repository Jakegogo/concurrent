package basesource.convertor.model;

import basesource.convertor.files.UserFileListener;
import basesource.convertor.files.monitor.FileAlterationMonitor;
import basesource.convertor.files.monitor.FileAlterationObserver;
import basesource.convertor.ui.FileTablePanel;
import basesource.convertor.ui.FileTreePanel;

import java.io.File;
import java.util.List;

/**
 * 列表更新通知接口
 * Created by Jake on 2015/5/31.
 */
public class ListableFileObservable {

    /** 文件浏览面板(左侧) */
    private FileTreePanel fileBrowserPanel;

    /** 文件列表面板(右侧) */
    private FileTablePanel fileListPanel;

    /** 文件管理器 */
    private ListableFileManager listableFileManager;

    /** 当前选中的目录 */
    private File curDirectory;

    /** 文件更新监视器 */
    private FileAlterationMonitor fileAlterationMonitor;


    public ListableFileObservable(FileTreePanel fileBrowserPanel, FileTablePanel fileListPanel, ListableFileManager listableFileManager, FileAlterationMonitor fileAlterationMonitor) {
        this.fileBrowserPanel = fileBrowserPanel;
        this.fileListPanel = fileListPanel;
        this.listableFileManager = listableFileManager;
        this.fileAlterationMonitor = fileAlterationMonitor;
    }


    /**
     * 更新表格数据
     * @param files
     */
    public void updateTableData(final File[] files) {
        this.fileListPanel.updateTableData(files);
    }


    /**
     * 更新选择文件夹
     */
    public void updateSelectDirectory() {
        if (this.curDirectory == null) {
            return;
        }
        this.updateSelectDirectory(curDirectory);
    }

    /**
     * 更新选择文件夹
     * @param file 文件夹
     */
    public void updateSelectDirectory(File file) {
        if (file == null) {
            return;
        }

        List<File> childFiles = listableFileManager.filterChildFiles(file);
        this.updateTableData(childFiles);

        // 更新文件监听器
        this.registerFileMonitor(file);
        this.curDirectory = file;
    }


    /**
     * 更新表格数据
     * @param fileList
     */
    public void updateTableData(final List<File> fileList) {
        final File[] files = fileList.toArray(new File[fileList.size()]);
        this.fileListPanel.updateTableData(files);
    }


    private void registerFileMonitor(File path) {
        if (!path.isDirectory()) {
            return;
        }

        if (path.equals(this.curDirectory)) {
            return;
        }

        // 清理文件监听器
        for (FileAlterationObserver observer : fileAlterationMonitor.getObservers()) {
            fileAlterationMonitor.removeObserver(observer);
        }

        FileAlterationObserver observer = new FileAlterationObserver(path, false);
        this.fileAlterationMonitor.addObserver(observer);
        observer.addListener(new UserFileListener(this));
        try {
            observer.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
