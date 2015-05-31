package basesource.gui;

import basesource.gui.contansts.DefaultUIConstant;
import basesource.gui.extended.AnimatingSplitPane;
import basesource.gui.utils.DpiUtils;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.View;

import javax.swing.*;
import java.awt.*;

/**
 * 主面板
 * Created by Jake on 2015/5/31.
 */
public class MainPanel extends SingleFrameApplication {

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
            ,"TextField.font"
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

    /**
     * 根面板
     */
    private JPanel rootPanel;

    /** 主面板 */
    private AnimatingSplitPane mainSplitPane;

    /** 右侧面板 */
    private AnimatingSplitPane rightPanel;

    /** 文件浏览面板(左侧) */
    private FileTreePanel fileBrowserPanel;

    /** 文件列表面板(右侧) */
    private FileTablePanel fileListPanel;

    /** 文件列表更新通知接口 */
    private ListableFileConnector listableFileConnector;

    public MainPanel(){}

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
        try {
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
        } catch (Exception e) {
        }

        //调整默认字体
        for (int i = 0; i < DEFAULT_FONT.length; i++) {
            UIManager.put(DEFAULT_FONT[i], DefaultUIConstant.DEFAULT_FONT);
        }

        // 导航条渐变颜色
        Color titleColor = UIManager.getColor("activeCaption");
        float[] hsb = Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
        UIManager.put("titleGradientColor1",
                Color.getHSBColor(hsb[0] - 0.013F, 0.15F, 0.85F));
        UIManager.put("titleGradientColor2",
                Color.getHSBColor(hsb[0] - 0.005F, 0.24F, 0.8F));

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



        FileTreePanel fileBrowserPanel = new FileTreePanel();
        fileBrowserPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
                ));
        mainSplitPane.setTopComponent(fileBrowserPanel);




        AnimatingSplitPane rightPanel = new AnimatingSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightPanel.setBorder(DefaultUIConstant.EMPTY_BORDER);
        mainSplitPane.setBottomComponent(rightPanel);



        FileTablePanel fileListPanel = new FileTablePanel();
        fileListPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH - DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_LIST_PANEL_HEIGHT)
        ));
        rightPanel.setTopComponent(fileListPanel);



        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(mainSplitPane, "Center");


        ListableFileConnector listableFileConnector = new ListableFileConnector(fileBrowserPanel, fileListPanel);
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
