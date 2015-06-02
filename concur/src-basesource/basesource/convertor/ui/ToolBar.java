package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.UserConfig;

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

    private JToggleButton startButton;

    private JToggleButton stopButton;

    private ListableFileManager listableFileManager;

    private boolean showStop = true;

    private ButtonGroup startStopButtonGroup;

    public ToolBar(ListableFileManager listableFileManager) {
        this.listableFileManager = listableFileManager;
        this.init();
    }

    public ToolBar(int horizontal, ListableFileManager listableFileManager) {
        super(horizontal);
        this.listableFileManager = listableFileManager;
        this.init();
    }

    private void init() {

        final JToggleButton saveButton = new JToggleButton();
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setText(DefaultUIConstant.SAVE_PATH_BUTTON);
        saveButton.setRolloverEnabled(true);
        saveButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setToolTipText("button with rollover image");

        final JFileChooser fileChooser = this.createFileChooser();
        this.add(saveButton);


        final ButtonGroup startStopButtonGroup = new ButtonGroup();

        final JToggleButton startButton = new JToggleButton();
        startButton.setBorderPainted(false);
        startButton.setIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setText(DefaultUIConstant.START_CONVERT_BUTTON);
        startButton.setRolloverEnabled(true);
        startButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setToolTipText("button with rollover image");
        startStopButtonGroup.add(startButton);
        this.add(startButton);


        final JToggleButton stopButton = new JToggleButton();
        stopButton.setBorderPainted(false);
        stopButton.setEnabled(false);
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setText(DefaultUIConstant.STOP_CONVERT_BUTTON);
        stopButton.setRolloverEnabled(true);
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setToolTipText("button with rollover image");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showStop) {
                    showCancel();
                } else {
                    showStop();
                    startStopButtonGroup.clearSelection();
                    stopButton.setEnabled(false);
                    saveButton.setEnabled(true);
                }
            }
        });
        startStopButtonGroup.add(stopButton);
        this.add(stopButton);


        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!showStop) {
                    showStop();
                }
                stopButton.setEnabled(true);
                saveButton.setEnabled(false);
            }
        });


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
        openButton.setEnabled(UserConfig.getInstance().getOutputPath() != null);
        this.add(openButton);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(saveButton);
                saveButton.setSelected(false);

                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    UserConfig.getInstance().changeOutPutPath(selectedFile);
                }
                openButton.setEnabled(UserConfig.getInstance().getOutputPath() != null);
            }
        });

        this.startButton = startButton;
        this.stopButton = stopButton;
        this.startStopButtonGroup = startStopButtonGroup;
    }

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
        return fileChooser;
    }


    private void showStop() {
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setText(DefaultUIConstant.STOP_CONVERT_BUTTON);
        stopButton.setSelected(false);
        showStop = true;
    }


    private void showCancel() {
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/cancel.png")));
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/cancel.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/cancel.png")));
        stopButton.setText(DefaultUIConstant.CANCLE_CONVERT_BUTTON);
        showStop = false;
    }


    /**
     * 重置开始按钮
     */
    public void resetStart() {
        startStopButtonGroup.clearSelection();
    }




}
