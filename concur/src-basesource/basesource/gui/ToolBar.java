package basesource.gui;

import basesource.gui.contansts.DefaultUIConstant;

import javax.swing.*;

/**
 * 工具条
 * Created by Jake on 2015/6/2.
 */
public class ToolBar extends JToolBar {

    public ToolBar() {
        this.init();
    }

    public ToolBar(int horizontal) {
        super(horizontal);
        this.init();
    }

    private void init() {


        JToggleButton saveButton = new JToggleButton();
        saveButton.setBorderPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setText(DefaultUIConstant.SAVE_PATH_BUTTON);
        saveButton.setRolloverEnabled(true);
        saveButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/save.png")));
        saveButton.setToolTipText("button with rollover image");
        this.add(saveButton);


        JToggleButton startButton = new JToggleButton();
        startButton.setBorderPainted(false);
        startButton.setContentAreaFilled(false);
        startButton.setIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setText(DefaultUIConstant.START_CONVERT_BUTTON);
        startButton.setRolloverEnabled(true);
        startButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/start.png")));
        startButton.setToolTipText("button with rollover image");
        this.add(startButton);


        JToggleButton stopButton = new JToggleButton();
        stopButton.setBorderPainted(false);
        stopButton.setContentAreaFilled(false);
        stopButton.setIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setText(DefaultUIConstant.STOP_CONVERT_BUTTON);
        stopButton.setRolloverEnabled(true);
        stopButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/stop.png")));
        stopButton.setToolTipText("button with rollover image");
        this.add(stopButton);


        JToggleButton openButton = new JToggleButton();
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        openButton.setIcon(new ImageIcon(getClass().getResource("resources/images/open.png")));
        openButton.setText(DefaultUIConstant.OPEN_CONVERT_BUTTON);
        openButton.setRolloverEnabled(true);
        openButton.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/open.png")));
        openButton.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/open.png")));
        openButton.setToolTipText("button with rollover image");
        this.add(openButton);

    }


}
