package basesource.convertor.ui.plaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Jake on 2015/6/6.
 */
public class Test {

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Test");

        final BufferedImage image = ImageIO.read(new URL(
                "http://upload.wikimedia.org/wikipedia/en/2/24/Lenna.png"));

        JTable table = new JTable(16, 3) {{
            setOpaque(false);
            setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {{
                setOpaque(false);
            }});
        }};

        frame.add(new JScrollPane(table) {{
            setOpaque(false);
            getViewport().setOpaque(false);
        }
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }

        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
