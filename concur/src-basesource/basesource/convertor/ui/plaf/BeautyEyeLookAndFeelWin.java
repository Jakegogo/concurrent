package basesource.convertor.ui.plaf;


import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
import org.jb2011.lnf.beautyeye.ch20_filechooser.__UI__;
import org.jb2011.lnf.beautyeye.winlnfutils.WinUtils;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.InsetsUIResource;


public class BeautyEyeLookAndFeelWin extends NimbusLookAndFeel {
    static {
        initLookAndFeelDecorated();
    }

    public BeautyEyeLookAndFeelWin() {
        BeautyEyeLNFHelper.implLNF();

        __UI__.uiImpl_win();

        initForVista();
    }

    protected void initForVista() {
        if (WinUtils.isOnVista()) {
            UIManager.put("CheckBoxMenuItem.margin", new InsetsUIResource(0, 0, 0, 0));
            UIManager.put("RadioButtonMenuItem.margin", new InsetsUIResource(0, 0, 0, 0));
            UIManager.put("Menu.margin", new InsetsUIResource(0, 0, 0, 0));
            UIManager.put("MenuItem.margin", new InsetsUIResource(0, 0, 0, 0));

            UIManager.put("Menu.border", new BorderUIResource(BorderFactory.createEmptyBorder(1, 3, 2, 3)));
            UIManager.put("MenuItem.border", new BorderUIResource(BorderFactory.createEmptyBorder(1, 0, 2, 0)));
            UIManager.put("CheckBoxMenuItem.border", new BorderUIResource(BorderFactory.createEmptyBorder(4, 2, 4, 2)));
            UIManager.put("RadioButtonMenuItem.border", new BorderUIResource(BorderFactory.createEmptyBorder(4, 0, 4, 0)));


        }
    }

    public String getName() {
        return "BeautyEyeWin";
    }

    public String getID() {
        return "BeautyEyeWin";
    }

    public String getDescription() {
        return "BeautyEye windows-platform L&F developed by Jack Jiang(jb2011@163.com).";
    }

    public boolean getSupportsWindowDecorations() {
        return false;
    }

    protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        initOtherResourceBundle(table);
    }

    protected void initOtherResourceBundle(UIDefaults table) {
        table.addResourceBundle("org.jb2011.lnf.beautyeye.resources.beautyeye");
    }

    static void initLookAndFeelDecorated() {
        if (BeautyEyeLNFHelper.frameBorderStyle == BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated) {
            JFrame.setDefaultLookAndFeelDecorated(false);
            JDialog.setDefaultLookAndFeelDecorated(false);
        } else {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }
    }
}

/* Location:           E:\java\beautyeye-3.5\src_all\SwingSets2_for_be_lnf\lib\beautyeye_lnf.jar
 * Qualified Name:     org.jb2011.lnf.beautyeye.BeautyEyeLookAndFeelWin
 * JD-Core Version:    0.6.2
 */