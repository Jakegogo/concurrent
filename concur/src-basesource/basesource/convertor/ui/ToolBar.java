package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;

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

    public ToolBar() {
        this.init();
    }

    public ToolBar(int horizontal) {
        super(horizontal);
        this.init();
    }

    private void init() {


        final JToggleButton saveButton = new JToggleButton();
        saveButton.setBorderPainted(false);
        saveButton.setIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setText(DefaultUIConstant.SAVE_PATH_BUTTON);
        saveButton.setRolloverEnabled(true);
        saveButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setToolTipText("button with rollover image");

        final JFileChooser fileChooser = this.createFileChooser();
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(saveButton);
                saveButton.setSelected(false);
            }
        });
        this.add(saveButton);


        ButtonGroup startStopButtonGroup = new ButtonGroup();

        JToggleButton startButton = new JToggleButton();
        startButton.setBorderPainted(false);
        startButton.setIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setText(DefaultUIConstant.START_CONVERT_BUTTON);
        startButton.setRolloverEnabled(true);
        startButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setToolTipText("button with rollover image");
        startStopButtonGroup.add(startButton);
        this.add(startButton);


        JToggleButton stopButton = new JToggleButton();
        stopButton.setBorderPainted(false);
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setText(DefaultUIConstant.STOP_CONVERT_BUTTON);
        stopButton.setRolloverEnabled(true);
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setToolTipText("button with rollover image");
        startStopButtonGroup.add(stopButton);
        this.add(stopButton);


        final JToggleButton openButton = new JToggleButton();
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
            }
        });
        this.add(openButton);


        this.startButton = startButton;
        this.stopButton = stopButton;
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
        return fileChooser;
    }


    /**
     * 重置开始按钮
     */
    public void resetStart() {
        this.startButton.setSelected(false);
        this.stopButton.setSelected(false);
    }




}
