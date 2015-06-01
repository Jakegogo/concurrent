package basesource.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;

import basesource.gui.contansts.DefaultUIConstant;
import basesource.gui.extended.RoundedTitleBorder;
import basesource.gui.utils.DpiUtils;

/**
 * 文件列表面板
 * Created by Jake on 2015/5/31.
 */
public class FileTablePanel extends JScrollPane {
	private static final long serialVersionUID = -5058150638044083539L;

	/** 文件列表表格 */
    private JTable fileTable;

    /** Table model for File[]. */
    private FileTableModel fileTableModel;

    private ListSelectionListener listSelectionListener;

    /** FileSystemView */
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();



    public FileTablePanel() {
        this.init();
    }

    // 初始化界面
    private void init() {
        setBorder(new RoundedTitleBorder(DefaultUIConstant.FILE_TABLE_TITLE,
                UIManager.getColor("titleGradientColor1"),
                UIManager.getColor("titleGradientColor2")));
        this.setViewportView(this.createFileTable());
    }


    /**
     * 创建文件列表
     * @return
     */
    private Component createFileTable() {
        JTable fileTable = new JTable();
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setAutoCreateRowSorter(true);

        this.bindListener(fileTable);

        this.fileTable = fileTable;
        return fileTable;
    }


    private void bindListener(final JTable fileTable) {
        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int row = fileTable.getSelectionModel().getLeadSelectionIndex();
                ((FileTableModel)fileTable.getModel()).getFile(row);
            }
        };
    }


    /** Update the table on the EDT */
    public void updateTableData(final File[] files) {
        if (files == null || files.length == 0) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (fileTableModel==null) {
                    fileTableModel = new FileTableModel();
                    fileTable.setModel(fileTableModel);
                }
                fileTable.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                fileTable.getSelectionModel().addListSelectionListener(listSelectionListener);

                Icon icon = fileSystemView.getSystemIcon(files[0]);
                if (icon.getIconHeight() > fileTable.getRowHeight()) {
                    // size adjustment to better account for icons
                    fileTable.setRowHeight(icon.getIconHeight() + DpiUtils.getDoubleDpiExtendedSize(DefaultUIConstant.FILE_TABLE_ROW_DEFAULT_PADDING));
                }
                setColumnWidth(0, icon.getIconWidth() + DpiUtils.getDoubleDpiExtendedSize(DefaultUIConstant.FILE_TABLE_ROW_DEFAULT_PADDING));
                convertToImageCell(0);
            }
        });
    }


    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = fileTable.getColumnModel().getColumn(column);
        if (width < 0) {
            // use the preferred width of the header..
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }


    private void convertToImageCell(int column) {
        TableColumn tableColumn = fileTable.getColumnModel().getColumn(column);
        tableColumn.setCellRenderer(new ImageIconTableCellRenderer());
    }


    static class ImageIconTableCellRenderer extends JXTable.IconRenderer {

        private static final long serialVersionUID = 1L;
        
        private JLabel label = new JLabel();
        
        ImageIconTableCellRenderer() {
        	label.setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        	label.setIcon((Icon) value);
            return label;
        }
    }

}
