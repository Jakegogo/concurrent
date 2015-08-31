package basesource.convertor.ui.extended;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import basesource.convertor.model.FolderInfo;

/**
 * 文件树渲染器
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 4179377782261072344L;

    private JLabel label;
    
    public FileTreeCellRenderer() {
        label = new JLabel();
        label.setOpaque(true);
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
        FolderInfo folderInfo = (FolderInfo) node.getUserObject();
        if (folderInfo == null) {
            return label;
        }

        label.setIcon(folderInfo.getIcon());
        label.setText(folderInfo.getName());
        label.setToolTipText(folderInfo.getPath());

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