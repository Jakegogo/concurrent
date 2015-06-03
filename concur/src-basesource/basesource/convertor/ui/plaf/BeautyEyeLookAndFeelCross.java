package basesource.convertor.ui.plaf;

import org.jb2011.lnf.beautyeye.ch20_filechooser.__UI__;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;


public class BeautyEyeLookAndFeelCross extends MetalLookAndFeel {
    static {
        BeautyEyeLookAndFeelWin.initLookAndFeelDecorated();
    }

    public BeautyEyeLookAndFeelCross() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);

        UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
        BeautyEyeLNFHelper.implLNF();

        __UI__.uiImpl_cross();
    }

    public String getName() {
        return "BeautyEyeCross";
    }

    public String getID() {
        return "BeautyEyeCross";
    }

    public String getDescription() {
        return "BeautyEye cross-platform L&F developed by Jack Jiang(jb2011@163.com).";
    }

    public boolean getSupportsWindowDecorations() {
        return true;
    }

    public boolean isNativeLookAndFeel() {
        return false;
    }

    public boolean isSupportedLookAndFeel() {
        return true;
    }

    protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        initOtherResourceBundle(table);
    }

    protected void initOtherResourceBundle(UIDefaults table) {
        table.addResourceBundle("org.jb2011.lnf.beautyeye.resources.beautyeye");
    }
}

/* Location:           E:\java\beautyeye-3.5\src_all\SwingSets2_for_be_lnf\lib\beautyeye_lnf.jar
 * Qualified Name:     org.jb2011.lnf.beautyeye.BeautyEyeLookAndFeelCross
 * JD-Core Version:    0.6.2
 */