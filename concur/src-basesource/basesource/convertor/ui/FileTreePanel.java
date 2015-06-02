package basesource.convertor.ui;

import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.ListableFileObservable;
import basesource.convertor.ui.extended.FileNode;
import basesource.convertor.ui.extended.FileTreeCellRenderer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.List;

/**
 * 左侧文件树浏览面板
 * Created by Jake on 2015/5/31.
 */
public class FileTreePanel extends JScrollPane {
	private static final long serialVersionUID = -5499094661570412734L;

    /** 文件树 */
    private JTree fileTree;

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
        this.listableFileManager = listableFileManager;
        this.init();
    }

    // 初始化界面
    private void init() {
        setViewportView(this.createFileTree());
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
        fileTree.setCellRenderer(new FileTreeCellRenderer(listableFileManager));
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

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                if (!node.hasInit()) {

                    File file = (File) node.getUserObject();
                    if (file == null) {
                        return null;
                    }

                    for (File childDirectorys : listableFileManager.listChildDirectorys(file)) {
                        this.publish(childDirectorys);
                    }

                    // 联动 更新 表格
                    List<File> childFiles = listableFileManager.listChildFiles(file);
                    listableFileConnector.updateTableData(childFiles);

                    node.setInit(true);
                }

                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    boolean hasChileDirectorys = listableFileManager.hasChildDirectorys(child);
                    FileNode childNode = new FileNode(child, hasChileDirectorys);
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
