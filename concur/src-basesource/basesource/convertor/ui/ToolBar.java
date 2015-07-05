package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.model.*;
import basesource.convertor.task.TaskStatus;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * 工具条
 * Created by Jake on 2015/6/2.
 */
public class ToolBar extends JToolBar {
	private static final long serialVersionUID = -8094727898194177964L;

	private JToggleButton startButton;

    private JToggleButton stopButton;

    private ListableFileManager listableFileManager;
    
    /** 文件列表更新通知接口 */
    private ListableFileObservable listableFileConnector;

    /** 任务管理器 */
    private TaskManager taskManager;

    private ButtonGroup startStopButtonGroup;
    
    private JToggleButton expandButton;

    public ToolBar(int horizontal, ListableFileObservable listableFileConnector, TaskManager taskManager) {
        super(horizontal);
        this.listableFileConnector = listableFileConnector;
        this.listableFileManager = listableFileConnector.getListableFileManager();
        this.taskManager = taskManager;
        this.init();
    }

    private void init() {

        // 构建保存按钮
        final JToggleButton saveButton = new JToggleButton() {


        };
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setText(DefaultUIConstant.SAVE_PATH_BUTTON);
        saveButton.setRolloverEnabled(true);
        saveButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setToolTipText("button with rollover image");

        // 保存按钮文件选择
        final JFileChooser fileChooser = this.createFileChooser();
        add(saveButton);


        // 构建开始暂停按钮组
        final ButtonGroup startStopButtonGroup = new ButtonGroup();

        // 构建开始按钮
        final JToggleButton startButton = new JToggleButton();
        startButton.setBorderPainted(false);
        startButton.setIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setText(DefaultUIConstant.START_CONVERT_BUTTON);
        startButton.setRolloverEnabled(true);
        startButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setToolTipText("button with rollover image");
        startStopButtonGroup.add(startButton);
        add(startButton);

        // 开始按钮默认开启状态
        File outPutPath = UserConfig.getInstance().getOutputPath();
        boolean chooseOutputPath = outPutPath != null && outPutPath.exists();
        startButton.setEnabled(chooseOutputPath);


        // 构建暂停按钮
        final JToggleButton stopButton = new JToggleButton();
        stopButton.setBorderPainted(false);
        stopButton.setEnabled(false);
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setText(DefaultUIConstant.STOP_CONVERT_BUTTON);
        stopButton.setRolloverEnabled(true);
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setToolTipText("button with rollover image");
        // 绑定暂停事件
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (taskManager.isStarted()) {
                    showCancel();
                    // 暂停任务
                    taskManager.stop();
                    startButton.setEnabled(true);
                } else if (taskManager.getStatus() == TaskStatus.STOPED) {
                    showStop();
                    startStopButtonGroup.clearSelection();
                    stopButton.setEnabled(false);
                    saveButton.setEnabled(true);
                    // 取消任务
                    taskManager.cancel();
                    startButton.setEnabled(true);
                }
            }
        });
        startStopButtonGroup.add(stopButton);
        add(stopButton);

        // 绑定开始事件
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 开始任务
                taskManager.start(new TaskStatusChangeCallback() {

                    @Override
                    public void onStart() {
                        if (taskManager.isStarted()) {
                            showStop();
                            startButton.setEnabled(false);
                            stopButton.setEnabled(true);
                            saveButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onComplete() {
                        showStop();
                        startStopButtonGroup.clearSelection();
                        stopButton.setEnabled(false);
                        saveButton.setEnabled(true);
                        startButton.setEnabled(true);
                    }
                });

            }
        });


        // 构建查看按钮
        final JToggleButton openButton = new JToggleButton();
        openButton.setFocusPainted(false);
        openButton.setBorderPainted(false);
        openButton.setIcon(new ImageIcon(getClass().getResource("resources/images/open.png")));
        openButton.setText(DefaultUIConstant.OPEN_CONVERT_BUTTON);
        openButton.setRolloverEnabled(true);
        openButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/open.png")));
        openButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/open.png")));
        openButton.setToolTipText("button with rollover image");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openButton.setSelected(false);
                listableFileManager.openFileView(UserConfig.getInstance().getOutputPath());
            }
        });
        openButton.setEnabled(chooseOutputPath);
        add(openButton);


        // 构建展开/折叠按钮
        final JToggleButton expandButton = new JToggleButton();
        expandButton.setFocusPainted(false);
        expandButton.setBorderPainted(false);
        expandButton.setIcon(new ImageIcon(getClass().getResource("resources/images/expand.png")));
        expandButton.setRolloverEnabled(true);
        expandButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/expand.png")));
        expandButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/expand.png")));
        expandButton.setToolTipText("button with rollover image");
        expandButton.setSelected(true);
        expandButton.addActionListener(new ActionListener() {
        	private boolean showExpand = true;
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (showExpand) {
            		showCollapse();
            		listableFileConnector.updateSize(1200, 800);
            	} else {
            		showExpand();
            		listableFileConnector.updateSize(0, 0);
            	}
            	showExpand = !showExpand;
            }
        });

        add(Box.createHorizontalGlue());
        add(expandButton);


        // 构建输入路径选择按钮
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int retval = fileChooser.showOpenDialog(saveButton);
                saveButton.setSelected(false);

                if (retval != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile == null || !selectedFile.exists()) {
                    return;
                }

                UserConfig.getInstance().changeOutPutPath(selectedFile);
                fileChooser.setCurrentDirectory(selectedFile);

                openButton.setEnabled(true);
                startButton.setEnabled(true);
            }
        });

        this.startButton = startButton;
        this.stopButton = stopButton;
        this.expandButton = expandButton;
        this.startStopButtonGroup = startStopButtonGroup;
    }

    /**
     * 创建文件选择器
     * @return
     */
    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return DefaultUIConstant.ACCECPT_FILE_LIMIT_TIP;
            }
        });
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录

        File dir = UserConfig.getInstance().getOutputPath();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            fileChooser.setCurrentDirectory(dir);
        }
        return fileChooser;
    }


    // 显示暂停按钮
    private void showStop() {
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setText(DefaultUIConstant.STOP_CONVERT_BUTTON);
        stopButton.setSelected(false);
    }


    // 显示取消按钮
    private void showCancel() {
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/cancel.png")));
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/cancel.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/cancel.png")));
        stopButton.setText(DefaultUIConstant.CANCLE_CONVERT_BUTTON);
    }
    
    // 显示展开按钮
    private void showExpand() {
    	expandButton.setIcon(new ImageIcon(getClass().getResource("resources/images/expand.png")));
    	expandButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/expand.png")));
    	expandButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/expand.png")));
    	expandButton.setSelected(true);
    }

    // 显示折叠按钮
    private void showCollapse() {
    	expandButton.setIcon(new ImageIcon(getClass().getResource("resources/images/collapse.png")));
    	expandButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/collapse.png")));
    	expandButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/collapse.png")));
    	expandButton.setSelected(true);
    }
    

    /**
     * 重置开始按钮
     */
    public void resetStart() {
        startStopButtonGroup.clearSelection();
    }

//    @Override
//    public void paint(Graphics g) {
//
//        SmoothUtilities.configureGraphics(g);
//
//        Insets insets = getInsets();
//        Rectangle bounds = getBounds();
//
//
//
//        super.paint(g);
//
//
//        g.setColor(DefaultUIConstant.TABLE_ROW_PROGRESS_BAR_COLOR2);
//        g.fillRect(insets.left, insets.top + bounds.height - 2, bounds.width, bounds.height);
//    }
}
