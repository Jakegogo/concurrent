package basesource.convertor.model;

import basesource.convertor.files.UserFileListener;
import basesource.convertor.files.monitor.FileAlterationMonitor;
import basesource.convertor.files.monitor.FileAlterationObserver;
import basesource.convertor.ui.FileTablePanel;
import basesource.convertor.ui.FileTreePanel;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * 列表更新通知接口
 * Created by Jake on 2015/5/31.
 */
public class ListableFileObservable {
	
	/** 根面板 */
    private JPanel rootPanel;

    /** 文件浏览面板(左侧) */
    private FileTreePanel fileBrowserPanel;

    /** 文件列表面板(右侧) */
    private FileTablePanel fileListPanel;

    /** 文件管理器 */
    private ListableFileManager listableFileManager;

    /** 当前选中的目录 */
    private FolderInfo curDirectory;

    /** 文件更新监视器 */
    private FileAlterationMonitor fileAlterationMonitor;

    /** 任务管理器 */
    private TaskManager taskManager;


    public ListableFileObservable(JPanel rootPanel, FileTreePanel fileBrowserPanel, FileTablePanel fileListPanel, ListableFileManager listableFileManager, FileAlterationMonitor fileAlterationMonitor, TaskManager taskManager) {
        this.fileBrowserPanel = fileBrowserPanel;
        this.fileListPanel = fileListPanel;
        this.listableFileManager = listableFileManager;
        this.fileAlterationMonitor = fileAlterationMonitor;
        this.rootPanel = rootPanel;
        this.taskManager = taskManager;
    }


    public ListableFileManager getListableFileManager() {
		return listableFileManager;
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
     * @param folderInfo 文件夹
     */
    public void updateSelectDirectory(FolderInfo folderInfo) {
        if (folderInfo == null) {
            return;
        }
        
        File file = folderInfo.getFileCache();
        if (!file.exists()) {
        	return;
        }

        // 保存到配置文件
        boolean changed = UserConfig.getInstance().changeInPutPath(file);
        if (changed) {
            // 修改任务进度
            this.taskManager.changeInputPath(file);
        }
        
        List<File> childFiles = listableFileManager.filterChildFiles(file);
        this.updateTableData(childFiles);

        // 更新文件监听器
        this.registerFileMonitor(file);
        this.curDirectory = folderInfo;
    }


    /**
     * 更新表格数据
     * @param fileList
     */
    public void updateTableData(final List<File> fileList) {
        final File[] files = fileList.toArray(new File[fileList.size()]);
        this.fileListPanel.updateTableData(files);
    }

    
    /**
     * 注册文件变化监听器
     * @param path
     */
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

    /**
     * 更新主面板尺寸
     * @param w
     * @param h
     */
    public void updateSize(int w, int h) {
    	this.rootPanel.setSize(w, h);
    	this.fileListPanel.setSize(w, h);
    }
    

}
