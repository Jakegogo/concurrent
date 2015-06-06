package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.model.FolderInfo;
import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.ListableFileObservable;
import basesource.convertor.ui.docking.demos.elegant.ElegantPanel;
import basesource.convertor.ui.extended.FileNode;
import basesource.convertor.ui.extended.FileTreeCellRenderer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

/**
 * 左侧文件树浏览面板
 * Created by Jake on 2015/5/31.
 */
public class FileTreePanel extends ElegantPanel {
	private static final long serialVersionUID = -5499094661570412734L;

    /** 文件树 */
    private JTree fileTree;

    /** fileTree包装容器 */
    private JScrollPane innerTreePanel;

    /** 文件管理器 */
    private ListableFileManager listableFileManager;

    /** 文件列表更新通知接口 */
    private ListableFileObservable listableFileConnector;

    /**
     * 设置文件列表更新通知实例
     * @param listableFileConnector ListableFileConnector
     */
    public void setListableFileConnector(ListableFileObservable listableFileConnector) {
        this.listableFileConnector = listableFileConnector;
    }

    public FileTreePanel(ListableFileManager listableFileManager) {
        super(DefaultUIConstant.FILE_TREE_PANEL_TITTLE);
        this.listableFileManager = listableFileManager;
        this.init();
    }

    // 初始化界面
    private void init() {
        JScrollPane innerTreePanel = new JScrollPane();
        super.add(innerTreePanel);
        innerTreePanel.setViewportView(this.createFileTree());
        this.innerTreePanel = innerTreePanel;

    }

    public void doLayout() {
        super.doLayout();
        Insets insets = getInsets();
        int w = getWidth()-insets.left-insets.right;
        int h = getHeight()-insets.top-insets.bottom;
        this.innerTreePanel.setBounds(insets.left, insets.top + 25, w, h);

    }

    /**
     * 初始化文件树
     */
    private JTree createFileTree() {

        // the File tree
        FileNode root = new FileNode();
        // 初始化默认文件夹节点
        listableFileManager.convertDefaultFileListToTreeNode(root);

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        JTree fileTree = new JTree(treeModel);
        fileTree.setRootVisible(false);
        fileTree.setCellRenderer(new FileTreeCellRenderer());
        fileTree.expandRow(0);

        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse){
                FileNode fileNode =
                        (FileNode) tse.getPath().getLastPathComponent();
                showChildren(fileNode);
            }

        };

        fileTree.addTreeSelectionListener(treeSelectionListener);

        this.fileTree = fileTree;
        return fileTree;
    }





    /** Add the files that are contained within the directory of this node.
     Thanks to Hovercraft Full Of Eels for the SwingWorker fix. */
    private void showChildren(final FileNode node) {
        this.fileTree.setEnabled(false);

        SwingWorker<Void, FolderInfo> worker = new SwingWorker<Void, FolderInfo>() {
            @Override
            public Void doInBackground() {
            	FolderInfo folderInfo = (FolderInfo) node.getUserObject();
                if (folderInfo == null) {
                    return null;
                }

                if (!node.hasInit()) {
                    for (FolderInfo childDirectorys : listableFileManager.listChildFolderInfo(folderInfo)) {
                        this.publish(childDirectorys);
                    }
                    node.setInit(true);
                }

                // 联动 更新 表格
                listableFileConnector.updateSelectDirectory(folderInfo);

                return null;
            }

            @Override
            protected void process(List<FolderInfo> chunks) {
                for (FolderInfo child : chunks) {
                    FileNode childNode = new FileNode(child);
                    node.add(childNode);
                }
            }

            @Override
            protected void done() {
                fileTree.setEnabled(true);
            }
        };
        // 提交执行
        worker.execute();
    }

}
