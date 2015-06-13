package basesource.convertor.ui.plaf;

import basesource.convertor.ui.extended.HintTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

  public static void main(String[] args) {

    final JFrame frame = new JFrame();

    frame.setLayout(new BorderLayout());

    final JTextField textFieldA = new HintTextField("A hint here");
    final JTextField textFieldB = new HintTextField("Another hint here");

    frame.add(textFieldA, BorderLayout.NORTH);
    frame.add(textFieldB, BorderLayout.CENTER);
    JButton btnGetText = new JButton("Get text");

    btnGetText.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String message = String.format("textFieldA='%s', textFieldB='%s'",
            textFieldA.getText(), textFieldB.getText());
        JOptionPane.showMessageDialog(frame, message);
      }
    });

    frame.add(btnGetText, BorderLayout.SOUTH);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setVisible(true);
    frame.pack();
  }
}

