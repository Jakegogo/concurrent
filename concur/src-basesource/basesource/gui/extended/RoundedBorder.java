
package basesource.gui.extended;

import javax.swing.border.Border;
import java.awt.*;


public class RoundedBorder
        implements Border {
    private int cornerRadius;

    public RoundedBorder() {
        this(10);
    }

    public RoundedBorder(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = (insets.bottom = this.cornerRadius / 2);
        insets.left = (insets.right = 1);
        return insets;
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = Utilities.deriveColorHSB(c.getBackground(), 0.0F, 0.0F, -0.3F);

        g2.setColor(Utilities.deriveColorAlpha(color, 40));
        g2.drawRoundRect(x, y + 2, width - 1, height - 3, this.cornerRadius, this.cornerRadius);
        g2.setColor(Utilities.deriveColorAlpha(color, 90));
        g2.drawRoundRect(x, y + 1, width - 1, height - 2, this.cornerRadius, this.cornerRadius);
        g2.setColor(Utilities.deriveColorAlpha(color, 255));
        g2.drawRoundRect(x, y, width - 1, height - 1, this.cornerRadius, this.cornerRadius);

        g2.dispose();
    }
}

/* Location:           E:\java\beautyeye-3.5\demo\excute_jar\SwingSets3(BeautyEyeLNFDemo) (1).jar
 * Qualified Name:     com.sun.swingset3.utilities.RoundedBorder
 * JD-Core Version:    0.6.2
 */