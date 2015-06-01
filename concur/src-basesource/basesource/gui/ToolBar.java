package basesource.gui;

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

        JButton button = new JButton();
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setIcon(new ImageIcon(getClass().getResource("resources/images/redbutton.png")));
        button.setRolloverEnabled(true);
        button.setRolloverIcon(new ImageIcon(getClass().getResource("resources/images/redbutton_glow.png")));
        button.setPressedIcon(new ImageIcon(getClass().getResource("resources/images/redbutton_dark.png")));
        button.setToolTipText("button with rollover image");

        this.add(button);
    }


}
