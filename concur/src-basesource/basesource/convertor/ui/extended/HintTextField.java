package basesource.convertor.ui.extended;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 带提示的输入框
 * Created by Jake on 2015/6/13.
 */
public class HintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean showingHint;

    public HintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        this.showingHint = true;
        super.addFocusListener(this);
        super.setForeground(Color.LIGHT_GRAY);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setForeground(Color.LIGHT_GRAY);
            super.setText("");
            showingHint = false;
        } else {
            super.setForeground(Color.BLACK);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setForeground(Color.LIGHT_GRAY);
            super.setText(hint);
            showingHint = true;
        } else {
            super.setForeground(Color.BLACK);
        }
    }

    @Override
    public String getText() {
        return showingHint ? "" : super.getText();
    }

}
