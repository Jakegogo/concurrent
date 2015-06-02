package basesource.convertor.ui.extended;

import basesource.convertor.model.ListableFileManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

/**
 * 文件树渲染器
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 4179377782261072344L;

    /** 文件管理器 */
    private ListableFileManager listableFileManager;

    private JLabel label;

    public FileTreeCellRenderer(ListableFileManager listableFileManager) {
        label = new JLabel();
        label.setOpaque(true);
        this.listableFileManager = listableFileManager;
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        File file = (File) node.getUserObject();
        if (file == null) {
            return label;
        }

        label.setIcon(listableFileManager.getSystemIcon(file));
        label.setText(listableFileManager.getSystemDisplayName(file));
        label.setToolTipText(file.getPath());

        if (selected) {
            label.setBackground(backgroundSelectionColor);
            label.setForeground(textSelectionColor);
        } else {
            label.setBackground(backgroundNonSelectionColor);
            label.setForeground(textNonSelectionColor);
        }

        return label;
    }

}