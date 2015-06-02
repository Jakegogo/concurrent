
package basesource.convertor.ui.extended;


import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;


public class Utilities {
	
    public static String getURLFileName(URL url) {
        String path = url.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static BufferedImage createCompatibleImage(int width, int height) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
    }

    public static BufferedImage createTranslucentImage(int width, int height) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, 3);
    }

    public static BufferedImage createGradientImage(int width, int height, Color gradient1, Color gradient2) {
        BufferedImage gradientImage = createCompatibleImage(width, height);
        GradientPaint gradient = new GradientPaint(0.0F, 0.0F, gradient1, 0.0F, height, gradient2, false);
        Graphics2D g2 = (Graphics2D) gradientImage.getGraphics();
        g2.setPaint(gradient);
        g2.fillRect(0, 0, width, height);
        g2.dispose();

        return gradientImage;
    }

    public static BufferedImage createGradientMask(int width, int height, int orientation) {
        BufferedImage gradient = new BufferedImage(width, height,
                2);
        Graphics2D g = gradient.createGraphics();
        GradientPaint paint = new GradientPaint(0.0F, 0.0F,
                new Color(1.0F, 1.0F, 1.0F, 1.0F),
                orientation == 0 ? width : 0.0F,
                orientation == 1 ? height : 0.0F,
                new Color(1.0F, 1.0F, 1.0F, 0.0F));
        g.setPaint(paint);
        g.fill(new Rectangle2D.Double(0.0D, 0.0D, width, height));

        g.dispose();
        gradient.flush();

        return gradient;
    }

    public static Color deriveColorAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    public static Color deriveColorHSB(Color base, float dH, float dS, float dB) {
        float[] hsb = Color.RGBtoHSB(
                base.getRed(), base.getGreen(), base.getBlue(), null);

        hsb[0] += dH;
        hsb[1] += dS;
        hsb[2] += dB;
        return Color.getHSBColor(
                hsb[0] > 1.0F ? 1.0F : hsb[0] < 0.0F ? 0.0F : hsb[0],
                hsb[1] > 1.0F ? 1.0F : hsb[1] < 0.0F ? 0.0F : hsb[1],
                hsb[2] > 1.0F ? 1.0F : hsb[2] < 0.0F ? 0.0F : hsb[2]);
    }

    public static String getHTMLColorString(Color color) {
        String red = Integer.toHexString(color.getRed());
        String green = Integer.toHexString(color.getGreen());
        String blue = Integer.toHexString(color.getBlue());

        return "#" + (
                red.length() == 1 ? "0" + red : red) + (
                green.length() == 1 ? "0" + green : green) + (
                blue.length() == 1 ? "0" + blue : blue);
    }

    public static void printColor(String key, Color color) {
        float[] hsb = Color.RGBtoHSB(
                color.getRed(), color.getGreen(),
                color.getBlue(), null);
        System.out.println(key + ": RGB=" +
                color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "  " +
                "HSB=" + String.format("%.0f%n", new Object[]{Float.valueOf(hsb[0] * 360.0F)}) + "," +
                String.format("%.3f%n", new Object[]{Float.valueOf(hsb[1])}) + "," +
                String.format("%.3f%n", new Object[]{Float.valueOf(hsb[2])}));
    }


    public static void expandTree(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandTree(tree, path);
            }
        }
        tree.expandPath(parent);
    }

}
