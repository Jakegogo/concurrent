package basesource.gui.extended;

import javax.swing.*;
import java.awt.*;

public class IconImageViewer extends JPanel {

    private java.awt.Image image;
    private int xCoordinate;
    private boolean stretched = true;
    private int yCoordinate;

    private int width;
    private int height;

    public IconImageViewer() {
    }

    public IconImageViewer(Image image) {
        this.image = image;
        this.width = 20;
        this.height = 20;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            if (isStretched()) {
                g.drawImage(image, xCoordinate, yCoordinate,
                        this.width , this.height, this);
            } else {
                g.drawImage(image, xCoordinate, yCoordinate, this);
            }
        }
    }

    public java.awt.Image getImage() {
        return image;
    }

    public void setImage(java.awt.Image image) {
        this.image = image;
        repaint();
    }

    public boolean isStretched() {
        return stretched;
    }

    public void setStretched(boolean stretched) {
        this.stretched = stretched;
        repaint();
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
        repaint();
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
        repaint();
    }
}