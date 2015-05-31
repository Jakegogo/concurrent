package basesource.gui.extended;

import org.jdesktop.swingx.JXPanel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JXPanel {
    private final int cornerRadius;
    private boolean contentAreaFilled;
    private transient RoundRectangle2D.Float roundBounds;

    public RoundedPanel() {
        this(10);
    }

    public RoundedPanel(LayoutManager layout) {
        this(layout, 10);
    }

    public RoundedPanel(int cornerRadius) {
        this(new FlowLayout(), cornerRadius);
    }

    public RoundedPanel(LayoutManager layout, int cornerRadius) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.roundBounds = new RoundRectangle2D.Float(0.0F, 0.0F, 0.0F, 0.0F,
                cornerRadius, cornerRadius);
        this.contentAreaFilled = true;
        setOpaque(false);
    }

    public void setContentAreaFilled(boolean contentFilled) {
        this.contentAreaFilled = contentFilled;
    }

    public boolean isContentAreaFilled() {
        return this.contentAreaFilled;
    }

    protected void paintComponent(Graphics g) {
        if (isContentAreaFilled()) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension size = getSize();
            this.roundBounds.width = size.width;
            this.roundBounds.height = size.height;
            g2.setColor(getBackground());
            g2.fill(this.roundBounds);
        }
        super.paintComponent(g);
    }

}

/* Location:           E:\java\beautyeye-3.5\demo\excute_jar\SwingSets3(BeautyEyeLNFDemo) (1).jar
 * Qualified Name:     com.sun.swingset3.utilities.RoundedPanel
 * JD-Core Version:    0.6.2
 */