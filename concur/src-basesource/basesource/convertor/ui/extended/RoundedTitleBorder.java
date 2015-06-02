
package basesource.convertor.ui.extended;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class RoundedTitleBorder extends RoundedBorder {
    private final String title;
    private final Color[] titleGradientColors;

    public RoundedTitleBorder(String title, Color titleGradientColor1, Color titleGradientColor2) {
        super(10);
        this.title = title;
        this.titleGradientColors = new Color[2];
        this.titleGradientColors[0] = titleGradientColor1;
        this.titleGradientColors[1] = titleGradientColor2;
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        Insets borderInsets = super.getBorderInsets(c, insets);
        borderInsets.top = getTitleHeight(c);
        return borderInsets;
    }

    protected int getTitleHeight(Component c) {
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        return (int) (metrics.getHeight() * 1.8D);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int titleHeight = getTitleHeight(c);

        BufferedImage titleImage = Utilities.createTranslucentImage(width, titleHeight);
        GradientPaint gradient = new GradientPaint(0.0F, 0.0F,
                this.titleGradientColors[0], 0.0F, titleHeight,
                this.titleGradientColors[1], false);
        Graphics2D g2 = (Graphics2D) titleImage.getGraphics();
        g2.setPaint(gradient);
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(Utilities.deriveColorHSB(
                this.titleGradientColors[1], 0.0F, 0.0F, -0.2F));
        g2.drawLine(x + 1, titleHeight - 1, width - 2, titleHeight - 1);
        g2.setColor(Utilities.deriveColorHSB(
                this.titleGradientColors[1], 0.0F, -0.5F, 0.5F));
        g2.drawLine(x + 1, titleHeight, width - 2, titleHeight);
        g2.setPaint(new GradientPaint(0.0F, 0.0F, new Color(0.0F, 0.0F, 0.0F, 1.0F),
                width, 0.0F, new Color(0.0F, 0.0F, 0.0F, 0.0F)));
        g2.setComposite(AlphaComposite.DstIn);
        g2.fillRect(x, y, width, titleHeight);
        g2.dispose();

        g.drawImage(titleImage, x, y, c);

        super.paintBorder(c, g, x, y, width, height);

        g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setFont(c.getFont());
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        g2.drawString(this.title, x + 8,
                y + (titleHeight - metrics.getHeight()) / 2 + metrics.getAscent());
        g2.dispose();
    }
}
