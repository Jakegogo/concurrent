package basesource.gui;

import java.io.File;

/**
 * 列表更新通知接口
 * Created by Jake on 2015/5/31.
 */
public class ListableFileConnector {

    /** 文件浏览面板(左侧) */
    private FileTreePanel fileBrowserPanel;

    /** 文件列表面板(右侧) */
    private FileTablePanel fileListPanel;

    public ListableFileConnector(FileTreePanel fileBrowserPanel, FileTablePanel fileListPanel) {
        this.fileBrowserPanel = fileBrowserPanel;
        this.fileListPanel = fileListPanel;
    }


    /**
     * 更新表格数据
     * @param files
     */
    public void updateTableData(final File[] files) {
        this.fileListPanel.updateTableData(files);
    }




}
