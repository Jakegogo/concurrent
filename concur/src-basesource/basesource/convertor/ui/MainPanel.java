package basesource.convertor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.View;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.files.monitor.FileAlterationMonitor;
import basesource.convertor.files.monitor.FileAlterationObserver;
import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.ListableFileObservable;
import basesource.convertor.ui.docking.DockingPort;
import basesource.convertor.ui.docking.demos.elegant.ElegantDockingPort;
import basesource.convertor.ui.extended.AnimatingSplitPane;
import basesource.convertor.utils.DpiUtils;

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
    
    /**
     * 根面板
     */
    private JPanel rootPanel;

    /** 工具条 */
    private JToolBar toolBar;

    /** 主面板 */
    private ElegantDockingPort mainDockingPanel;

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
    
    public static void main(String[] args) {
    	new MainPanel(args);
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

        ElegantDockingPort mainDockingPanel = new ElegantDockingPort();
        mainDockingPanel.setSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
        ));
        mainDockingPanel.setBorder(DefaultUIConstant.EMPTY_BORDER);


        FileTreePanel fileBrowserPanel = new FileTreePanel(this.listableFileManager);
        fileBrowserPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
                ));
        mainDockingPanel.add(fileBrowserPanel, DockingPort.WEST_REGION);



        FileTablePanel fileListPanel = new FileTablePanel(this.listableFileManager);
        fileListPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH - DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_LIST_PANEL_HEIGHT)
        ));
        mainDockingPanel.add(fileListPanel, DockingPort.EAST_REGION);



        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
        ));
        rootPanel.add(mainDockingPanel, "Center");


        ListableFileObservable listableFileConnector = new ListableFileObservable(rootPanel, fileBrowserPanel, fileListPanel, listableFileManager, fileAlterationMonitor);
        fileBrowserPanel.setListableFileConnector(listableFileConnector);


        ToolBar toolBar = new ToolBar(JToolBar.HORIZONTAL, listableFileConnector);
        JPanel toolBarPanel = new JPanel(new BorderLayout());
        toolBarPanel.add(toolBar, "Center");
        rootPanel.add(toolBarPanel, "North");
        
        
        this.fileBrowserPanel = fileBrowserPanel;
        this.fileListPanel = fileListPanel;
        this.mainDockingPanel = mainDockingPanel;
        this.rootPanel = rootPanel;
        this.listableFileConnector = listableFileConnector;
        
        return rootPanel;
    }




}
