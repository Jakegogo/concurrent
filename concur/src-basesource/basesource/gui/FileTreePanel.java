package basesource.gui;

import basesource.gui.extended.FileTreeCellRenderer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 左侧文件树浏览面板
 * Created by Jake on 2015/5/31.
 */
public class FileTreePanel extends JScrollPane {

    FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    /** 文件树 */
    private JTree fileTree;

    /** 文件列表更新通知接口 */
    private ListableFileConnector listableFileConnector;

    /**
     * 设置文件列表更新通知实例
     * @param listableFileConnector ListableFileConnector
     */
    public void setListableFileConnector(ListableFileConnector listableFileConnector) {
        this.listableFileConnector = listableFileConnector;
    }

    public FileTreePanel() {
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
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        // 初始化默认文件夹节点
        this.getDefaultFileList(root);

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        JTree fileTree = new JTree(treeModel);
        fileTree.setRootVisible(false);
        fileTree.setCellRenderer(new FileTreeCellRenderer(fileSystemView));
        fileTree.expandRow(0);

        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse){
                DefaultMutableTreeNode fileNode =
                        (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
                showChildren(fileNode);
            }

        };

        fileTree.addTreeSelectionListener(treeSelectionListener);

        this.fileTree = fileTree;



        return fileTree;
    }


    /**
     * 获取默认文件选择列表
     * @param root
     */
    private void getDefaultFileList(DefaultMutableTreeNode root) {

        // show the file system roots. 显示全部文件夹
        File[] roots = fileSystemView.getRoots();
        for (File fileSystemRoot : roots) {

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
            root.add(node);

            File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            for (File file : files) {
                if (file.isDirectory()) {
                    DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file);
                    node.add(fileNode);
                }
            }
        }

    }


    /** Add the files that are contained within the directory of this node.
     Thanks to Hovercraft Full Of Eels for the SwingWorker fix. */
    private void showChildren(final DefaultMutableTreeNode node) {
        this.fileTree.setEnabled(false);

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                if (file.isDirectory()) {
                    List<File> fileList = new ArrayList<File>();
                    File[] files = fileSystemView.getFiles(file, true); //!!
                    if (node.isLeaf()) {
                        for (File child : files) {
                            if (child.isDirectory()) {
                                publish(child);
                                continue;
                            }
                            fileList.add(child);
                        }
                    }
                    // 联动 更新 表格
                    listableFileConnector.updateTableData(fileList.toArray(new File[fileList.size()]));
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
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
