package basesource.convertor.ui.plaf;

import org.jb2011.lnf.beautyeye.utils.JVM;
import org.jb2011.lnf.beautyeye.utils.Platform;
import org.jb2011.lnf.beautyeye.widget.border.BEShadowBorder;
import org.jb2011.lnf.beautyeye.widget.border.BEShadowBorder3;
import org.jb2011.lnf.beautyeye.widget.border.PlainGrayBorder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


public class BeautyEyeLNFHelper {
    public static boolean debug = false;

    public static boolean translucencyAtFrameInactive = true;

    public static FrameBorderStyle frameBorderStyle = isSurportedTranslucency() ? FrameBorderStyle.translucencyAppleLike : FrameBorderStyle.generalNoTranslucencyShadow;

    public static Color activeCaptionTextColor = new Color(0, 0, 0);

    public static Color commonBackgroundColor = new Color(250, 250, 250);

    public static Color commonForegroundColor = new Color(60, 60, 60);

    public static Color commonFocusedBorderColor = new Color(162, 162, 162);

    public static Color commonDisabledForegroundColor = new Color(172, 168, 153);

    public static Color commonSelectionBackgroundColor = new Color(2, 129, 216);

    public static Color commonSelectionForegroundColor = new Color(255, 255, 255);

    public static boolean setMaximizedBoundForFrame = true;

    protected static void implLNF() {
        org.jb2011.lnf.beautyeye.ch1_titlepane.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch2_tab.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch3_button.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch_x.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch4_scroll.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch5_table.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch6_textcoms.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch7_popup.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch8_toolbar.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch9_menu.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch10_internalframe.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch12_progress.__UI__.uiImpl();

//     org.jb2011.lnf.beautyeye.ch13_radio$cb_btn.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch14_combox.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch15_slider.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch16_tree.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch17_split.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch18_spinner.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch19_list.__UI__.uiImpl();

        org.jb2011.lnf.beautyeye.ch20_filechooser.__UI__.uiImpl();
    }

    public static String getBeautyEyeLNFStrCrossPlatform() {
        return "org.jb2011.lnf.beautyeye.BeautyEyeLookAndFeelCross";
    }

    public static String getBeautyEyeLNFStrWindowsPlatform() {
        return "org.jb2011.lnf.beautyeye.BeautyEyeLookAndFeelWin";
    }

    public static LookAndFeel getBeautyEyeLNFCrossPlatform() {
        return new basesource.convertor.ui.plaf.BeautyEyeLookAndFeelCross();
    }

    public static LookAndFeel getBeautyEyeLNFWindowsPlatform() {
        return new org.jb2011.lnf.beautyeye.BeautyEyeLookAndFeelWin();
    }

    public static void launchBeautyEyeLNF()
            throws Exception {
        if (Platform.isWindows()) {
            if (debug)
                System.out.println("已智能启用Windows平台专用的BeautyEye外观实现(您也可自行启用跨平台实现).");
            UIManager.setLookAndFeel(getBeautyEyeLNFStrWindowsPlatform());
        } else {
            if (debug)
                System.out.println("已智能启用跨平台的通用BeautyEye外观实现.");
            UIManager.setLookAndFeel(getBeautyEyeLNFStrCrossPlatform());
        }
    }

    public static boolean isSurportedTranslucency() {
        return JVM.current().isOneDotSixUpdate12OrAfter();
    }

    public static boolean __isFrameBorderOpaque() {
        return (frameBorderStyle == FrameBorderStyle.osLookAndFeelDecorated) ||
                (frameBorderStyle == FrameBorderStyle.generalNoTranslucencyShadow);
    }

    public static Border __getFrameBorder() {
        switch (frameBorderStyle.ordinal()) {
            case 1:
                return BorderFactory.createEmptyBorder();
            case 2:
                return new BEShadowBorder3();
            case 3:
                return new BEShadowBorder();
            case 4:
        }
        return new PlainGrayBorder();
    }

    public static enum FrameBorderStyle {
        osLookAndFeelDecorated,

        translucencyAppleLike,

        translucencySmallShadow,

        generalNoTranslucencyShadow;
    }

    public static abstract interface __UseParentPaintSurported {
        public abstract boolean isUseParentPaint();
    }
}

/* Location:           E:\java\beautyeye-3.5\src_all\SwingSets2_for_be_lnf\lib\beautyeye_lnf.jar
 * Qualified Name:     org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper
 * JD-Core Version:    0.6.2
 */