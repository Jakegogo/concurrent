package basesource.gui;

import basesource.gui.contansts.DefaultUI;
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

    /** 主面板 */
    private AnimatingSplitPane mainSplitPane;

    /** 右侧面板 */
    private AnimatingSplitPane rightPanel;

    /** 文件浏览面板(左侧) */
    private FileBrowserPanel fileBrowserPanel;


    public MainPanel(){}

    public MainPanel(String[] args) {
        launch(MainPanel.class, args);
    }


    @Override
    protected void startup() {
        this.initConfig();

        View view = getMainView();

        view.setComponent(createMainPanel());

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
            UIManager.put(DEFAULT_FONT[i], new Font("微软雅黑", Font.PLAIN, DpiUtils.getDpiExtendedSize(14)));
        }

    }


    /**
     * 创建主面板
     * @return
     */
    private JComponent createMainPanel() {

        this.mainSplitPane = new AnimatingSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.mainSplitPane.setBorder(DefaultUI.EMPTY_BORDER);
        this.mainSplitPane.setSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUI.DEFAULT_WIDTH),
                DpiUtils.getDpiExtendedSize(DefaultUI.DEFAULT_HEIGHT)
                ));



        this.fileBrowserPanel = new FileBrowserPanel();
        this.fileBrowserPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUI.DEFAULT_FILE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUI.DEFAULT_HEIGHT)
                ));
        this.mainSplitPane.setTopComponent(this.fileBrowserPanel);



        this.rightPanel = new AnimatingSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.rightPanel.setBorder(DefaultUI.EMPTY_BORDER);
        this.mainSplitPane.setBottomComponent(this.rightPanel);


        return this.mainSplitPane;
    }




}
