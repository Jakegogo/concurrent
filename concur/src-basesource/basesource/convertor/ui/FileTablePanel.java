package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.ProgressTableModel;
import basesource.convertor.task.TaskInfo;
import basesource.convertor.ui.docking.demos.elegant.ElegantPanel;
import basesource.convertor.utils.DpiUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * 文件列表面板
 * Created by Jake on 2015/5/31.
 */
public class FileTablePanel extends ElegantPanel {
	private static final long serialVersionUID = -5058150638044083539L;

	/** 文件列表表格 */
    private JTable fileTable;

    /** fileTable包装容器 */
    private JScrollPane innerTablePanel;

    /** Table model for File[]. */
    private ProgressTableModel fileTableModel;
    
    /** 选择行事件 */
    private ListSelectionListener listSelectionListener;
    
    /** 双击打开文件事件 */
    private MouseListener rowClickMouseListener;

    /** 文件管理器 */
    private ListableFileManager listableFileManager;

    /** 图标表格渲染器 */
    private ImageIconTableCellRenderer iconTableCellRenderer;


    public FileTablePanel(ListableFileManager listableFileManager) {
        super(DefaultUIConstant.FILE_TABLE_TITLE);
        this.listableFileManager = listableFileManager;
        this.init();
    }

    // 初始化界面
    private void init() {
//        setBorder(new RoundedTitleBorder(DefaultUIConstant.FILE_TABLE_TITLE,
//                UIManager.getColor("titleGradientColor1"),
//                UIManager.getColor("titleGradientColor2")));
        // 包装一层滚动面板
        JScrollPane innerTablePanel = new JScrollPane();
        innerTablePanel.setViewportView(this.createFileTable());
        innerTablePanel.setOpaque(false);
        innerTablePanel.getViewport().setOpaque(false);

        // 添加到容器
        super.add(innerTablePanel);

        this.innerTablePanel = innerTablePanel;
        this.setOpaque(false);
        this.setBackground(Color.WHITE);

        // 初始化表格数据模型
        fileTableModel = new ProgressTableModel(fileTable, innerTablePanel);
        fileTable.setModel(fileTableModel);

        // 绑定事件
        this.bindListener(fileTable);
    }

    public void doLayout() {
        super.doLayout();
        Insets insets = getInsets();
        int w = getWidth()-insets.left-insets.right;
        int h = getHeight()-insets.top-insets.bottom - 27;
        this.innerTablePanel.setBounds(insets.left, insets.top + 25, w, h);
    }
    /**
     * 创建文件列表
     * @return
     */
    private Component createFileTable() {
        final JTable fileTable = new JTable() {
//            @Override
//            public JToolTip createToolTip() {
//                JCustomTooltip tip = new JCustomTooltip(this, new JPanel());
//                tip.setPersistent(true);
//                tip.setComponent(this);
//                return tip;
//            }
        };
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setAutoCreateRowSorter(true);
        fileTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        fileTable.setGridColor(new Color(238, 238, 238));

        this.fileTable = fileTable;
        return fileTable;
    }


    /**
     * 绑定表格事件
     * @param fileTable JTable
     */
    private void bindListener(final JTable fileTable) {
        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
            	int viewRow = fileTable.getSelectedRow();
            	if (viewRow >= 0) {
        	        int row = fileTable.convertRowIndexToModel(viewRow);
        	        File file = ((ProgressTableModel) fileTable.getModel()).getFile(row);
            	}
            }
        };
        
        rowClickMouseListener = new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					doOpenFile(fileTable);
				}
			}
        };
        fileTable.addMouseListener(rowClickMouseListener);


        //悬浮提示单元格的值
        fileTable.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {

                // 展示任务信息
                int row = fileTable.rowAtPoint(e.getPoint());
                int col = fileTable.columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    int modelRow = fileTable.convertRowIndexToModel(row);
                    if (fileTableModel.isFail(modelRow)) {
                        TaskInfo taskInfo = fileTableModel.getTaskInfo(modelRow);
                        if (taskInfo != null) {
                            fileTable.setToolTipText(taskInfo.toString());//悬浮显示单元格内容
                        } else {
                            fileTable.setToolTipText(null);//关闭提示
                        }
                    } else {
                        Object value = fileTable.getValueAt(row, col);
                        if (null != value && !"".equals(value)) {
                            fileTable.setToolTipText(value.toString());//悬浮显示单元格内容
                        } else {
                            fileTable.setToolTipText(null);//关闭提示
                        }
                    }
                }
            }
        });

    }


    /**
     * 打开当前选择的文件
     * @param fileTable JTable
     */
    private void doOpenFile(final JTable fileTable) {
    	int viewRow = fileTable.getSelectedRow();
    	if (viewRow >= 0) {
	        int row = fileTable.convertRowIndexToModel(viewRow);
			File file = ((ProgressTableModel) fileTable.getModel()).getFile(row);
	        listableFileManager.openFileView(file);
    	}
	}


    /** Update the table on the EDT */
    public void updateTableData(final File[] files) {
        if (files == null) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                fileTable.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);

                if (files.length == 0) {
                    return;
                }

                fileTable.getSelectionModel().addListSelectionListener(listSelectionListener);

                Icon icon = listableFileManager.getSystemIcon(files[0]);
                if (icon.getIconHeight() > fileTable.getRowHeight()) {
                    // size adjustment to better account for icons
                    fileTable.setRowHeight(DpiUtils.addLineVerticalPadding(icon.getIconHeight()));
                }
                setColumnWidth(0, DpiUtils.addLineHorizontalPadding(icon.getIconWidth()));
                convertToImageCell(0);
            }
        });
    }

    /**
     * 设定列宽度
     * @param column 列编号
     * @param width 宽度
     */
    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = fileTable.getColumnModel().getColumn(column);
        if (width < 0) {
            // use the preferred width of the header..
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int) preferred.getWidth() + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    // 设定图标列渲染
    private void convertToImageCell(int column) {
        TableColumn tableColumn = fileTable.getColumnModel().getColumn(column);
        tableColumn.setCellRenderer(iconTableCellRenderer != null
                ? iconTableCellRenderer
                : (iconTableCellRenderer = new ImageIconTableCellRenderer()));
    }

    public ProgressTableModel getFileTableModel() {
        return fileTableModel;
    }

    /**
     * 文件图标显示渲染器
     */
    static class ImageIconTableCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        
        private JLabel label = new JLabel();
        
        ImageIconTableCellRenderer() {
        	label.setOpaque(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            label.setIcon((Icon) value);
            return label;
        }

    }

}
