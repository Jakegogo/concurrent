package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.files.monitor.FileAlterationMonitor;
import basesource.convertor.files.monitor.FileAlterationObserver;
import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.ListableFileObservable;
import basesource.convertor.ui.extended.AnimatingSplitPane;
import basesource.convertor.utils.DpiUtils;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.View;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 主面板
 * Created by Jake on 2015/5/31.
 */
public class MainPanel extends SingleFrameApplication {

    private static final String FALLBACK_FONT_FAMILY_NAME = Font.SANS_SERIF;
    private static final Map<String, String> FONT_FAMILY_NAMES = new HashMap<String, String>();
    private static final String[] BEST_FONT_FAMILIES = {
            "微软雅黑", "arial", "sans-serif"
    };

    /**
     * UIManager中UI字体相关的key
     */
    public static String[] DEFAULT_FONT = new String[]{
            "Table.font"
            , "TableHeader.font"
            , "CheckBox.font"
            , "Tree.font"
            , "Viewport.font"
            , "ProgressBar.font"
            , "RadioButtonMenuItem.font"
            , "ToolBar.font"
            , "ColorChooser.font"
            , "ToggleButton.font"
            , "Panel.font"
            , "TextArea.font"
            , "Menu.font"
            , "TableHeader.font"
            , "TextField.font"
            , "OptionPane.font"
            , "MenuBar.font"
            , "Button.font"
            , "Label.font"
            , "PasswordField.font"
            , "ScrollPane.font"
            , "MenuItem.font"
            , "ToolTip.font"
            , "List.font"
            , "EditorPane.font"
            , "Table.font"
            , "TabbedPane.font"
            , "RadioButton.font"
            , "CheckBoxMenuItem.font"
            , "TextPane.font"
            , "PopupMenu.font"
            , "TitledBorder.font"
            , "ComboBox.font"
    };
    
    static {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilyNames = env.getAvailableFontFamilyNames();
        for (String fontFamilyName : fontFamilyNames) {
            FONT_FAMILY_NAMES.put(fontFamilyName.toLowerCase(), fontFamilyName);
        }
        if (!FONT_FAMILY_NAMES.containsKey("serif")) {
            FONT_FAMILY_NAMES.put("serif", Font.SERIF);
        }
        if (!FONT_FAMILY_NAMES.containsKey("sans-serif")) {
            FONT_FAMILY_NAMES.put("sans-serif", Font.SANS_SERIF);
        }
    }
 
    public static void enableAntiAliasing() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }
 
    public static String getLookAndFeel() {
        return "basesource.convertor.ui.plaf.BeautyEyeLookAndFeelWin";
////        return "basesource.convertor.ui.plaf.nimbus.NimbusLookAndFeel";
//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    return info.getClassName();
//                }
//            }
//        } catch (Exception ignore) {
//        }
//        return UIManager.getCrossPlatformLookAndFeelClassName();
    }
 
    public static String getFontFamily(String[] fontFamilies) {
        for (String fontFamily : fontFamilies) {
            fontFamily = fontFamily.toLowerCase();
            if (FONT_FAMILY_NAMES.containsKey(fontFamily)) {
                return FONT_FAMILY_NAMES.get(fontFamily);
            }
        }
        return FALLBACK_FONT_FAMILY_NAME;
    }
 
    public static String[] getBestFontFamilies() {
        return BEST_FONT_FAMILIES;
    }
 
    public static int getBestFontSize() {
        return DefaultUIConstant.DEFAULT_FONT_SIZE;
    }
 
    /*########################################*/
 
    public static void setUI() {
        enableAntiAliasing();
//        // set LookAndFeel
        try {
            UIManager.setLookAndFeel(getLookAndFeel());
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        // set DefaultFont
        String bestFontFamily = getFontFamily(getBestFontFamilies());
        for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
            if (entry.getValue() instanceof FontUIResource) {
                FontUIResource fontUIRes = (FontUIResource) entry.getValue();
                entry.setValue(DefaultUIConstant.DEFAULT_FONT
                 );
            }
        }
//
        //调整默认字体
        for (int i = 0; i < DEFAULT_FONT.length; i++) {
            UIManager.put(DEFAULT_FONT[i], DefaultUIConstant.DEFAULT_FONT);
        }

    }
    
    
    
    /**
     * 根面板
     */
    private JPanel rootPanel;

    /** 工具条 */
    private JToolBar toolBar;

    /** 主面板 */
    private AnimatingSplitPane mainSplitPane;

    /** 右侧面板 */
    private AnimatingSplitPane rightPanel;

    /** 文件浏览面板(左侧) */
    private FileTreePanel fileBrowserPanel;

    /** 文件列表面板(右侧) */
    private FileTablePanel fileListPanel;

    /** 文件列表更新通知接口 */
    private ListableFileObservable listableFileConnector;

    /** 文件管理器 */
    private ListableFileManager listableFileManager = new ListableFileManager();

    /** 文件更新监视器 */
    private FileAlterationMonitor fileAlterationMonitor = new FileAlterationMonitor(DefaultUIConstant.FILE_MONITOR_INTERVAL);

    public MainPanel() {
    }

    public MainPanel(String[] args) {
        launch(MainPanel.class, args);
    }


    @Override
    protected void startup() {
        this.initConfig();

        View view = getMainView();

        view.setComponent(createRootPanel());

        show(view);
    }

    /**
     * 初始化配置
     */
    private void initConfig() {

//        try {
//            basesource.convertor.ui.plaf.BeautyEyeLNFHelper.launchBeautyEyeLNF();
//        } catch (Exception e) {
//        }

        // 设置UI
        setUI();

        // 导航条渐变颜色
        Color titleColor = UIManager.getColor("activeCaption");
        float[] hsb = Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
        UIManager.put("titleGradientColor1",
                Color.getHSBColor(hsb[0] - 0.013F, 0.15F, 0.85F));
        UIManager.put("titleGradientColor2",
                Color.getHSBColor(hsb[0] - 0.005F, 0.24F, 0.8F));
        
        //设置此开关量为false即表示关闭之，BeautyEye LNF中默认是true 
        BeautyEyeLNFHelper.translucencyAtFrameInactive = false;

        // 开启文件监听器
        for (FileAlterationObserver observer : fileAlterationMonitor.getObservers()) {
            fileAlterationMonitor.removeObserver(observer);
        }
        try {
            fileAlterationMonitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 创建主面板
     * @return
     */
    private JComponent createRootPanel() {

        AnimatingSplitPane mainSplitPane = new AnimatingSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setBorder(DefaultUIConstant.EMPTY_BORDER);
        mainSplitPane.setSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
                ));


        FileTreePanel fileBrowserPanel = new FileTreePanel(this.listableFileManager);
        fileBrowserPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
                ));
        mainSplitPane.setTopComponent(fileBrowserPanel);




        AnimatingSplitPane rightPanel = new AnimatingSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightPanel.setBorder(DefaultUIConstant.EMPTY_BORDER);
        mainSplitPane.setBottomComponent(rightPanel);



        FileTablePanel fileListPanel = new FileTablePanel(this.listableFileManager);
        fileListPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH - DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_LIST_PANEL_HEIGHT)
        ));
        rightPanel.setTopComponent(fileListPanel);



        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(mainSplitPane, "Center");


        ToolBar toolBar = new ToolBar(JToolBar.HORIZONTAL, this.listableFileManager);
        JPanel toolBarPanel = new JPanel(new BorderLayout());
        toolBarPanel.add(toolBar, "Center");
        rootPanel.add(toolBarPanel, "North");


        ListableFileObservable listableFileConnector = new ListableFileObservable(fileBrowserPanel, fileListPanel, listableFileManager, fileAlterationMonitor);
        fileBrowserPanel.setListableFileConnector(listableFileConnector);


        this.fileBrowserPanel = fileBrowserPanel;
        this.rightPanel = rightPanel;
        this.fileListPanel = fileListPanel;
        this.mainSplitPane = mainSplitPane;
        this.rootPanel = rootPanel;
        this.listableFileConnector = listableFileConnector;

        return this.rootPanel;
    }




}
