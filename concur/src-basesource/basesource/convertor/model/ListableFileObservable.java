package basesource.convertor.model;

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

    public ListableFileObservable(FileTreePanel fileBrowserPanel, FileTablePanel fileListPanel) {
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


    /**
     * 更新表格数据
     * @param fileList
     */
    public void updateTableData(final List<File> fileList) {
        final File[] files = fileList.toArray(new File[fileList.size()]);
        this.fileListPanel.updateTableData(files);
    }


}
