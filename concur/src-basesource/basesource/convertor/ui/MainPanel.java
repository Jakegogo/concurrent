package basesource.convertor.ui;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.files.monitor.FileAlterationMonitor;
import basesource.convertor.files.monitor.FileAlterationObserver;
import basesource.convertor.model.ListableFileManager;
import basesource.convertor.model.ListableFileObservable;
import basesource.convertor.model.TaskManager;
import basesource.convertor.ui.docking.DockingPort;
import basesource.convertor.ui.docking.demos.elegant.ElegantDockingPort;
import basesource.convertor.ui.extended.CustomFrameApplication;
import basesource.convertor.ui.extended.RowProgressTableUI;
import basesource.convertor.utils.DpiUtils;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import org.jdesktop.application.View;

import javax.swing.*;
import java.awt.*;

/**
 * 主面板
 * Created by Jake on 2015/5/31.
 */
public class MainPanel extends CustomFrameApplication {

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

    /** 任务管理器 */
    private TaskManager taskManager;

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
     * 在show之前
     */
    @Override
    protected void postInit() {
        // 默认窗口大小
        this.getMainFrame().setSize(1200, 800);

        // 设置窗口居中
        Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
        Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
        int screenWidth = screenSize.width / 2; // 获取屏幕的宽
        int screenHeight = screenSize.height / 2; // 获取屏幕的高
        int height = this.getMainFrame().getHeight();
        int width = this.getMainFrame().getWidth();
        this.getMainFrame().setLocation(screenWidth - width / 2, screenHeight - height / 2);
    }


    /**
     * 初始化配置
     */
    private void initConfig() {

        try {
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
        } catch (Exception e) {}


        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TableUI", RowProgressTableUI.class.getName());


        //调整默认字体
        for (int i = 0; i < DefaultUIConstant.DEFAULT_FONT_COMPONENT.length; i++) {
            UIManager.put(DefaultUIConstant.DEFAULT_FONT_COMPONENT[i], DefaultUIConstant.DEFAULT_FONT);
        }

        // 导航条渐变颜色
        Color titleColor = UIManager.getColor("activeCaption");
        float[] hsb = Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
        UIManager.put("titleGradientColor1",
                Color.getHSBColor(hsb[0] - 0.013F, 0.15F, 0.85F));
        UIManager.put("titleGradientColor2",
                Color.getHSBColor(hsb[0] - 0.005F, 0.24F, 0.8F));

        // 设置面板
        UIManager.put("BETitlePane.optionPanel", new OptionPanel());

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

        // 更改图标
        this.changeIcon();

    }


    // 更改图标
    private void changeIcon() {
        this.getMainFrame().setIconImage(
                new ImageIcon(getClass().getResource("resources/images/icon.png")).getImage());
        this.getMainFrame().setTitle(DefaultUIConstant.CONVERTOR_TITLE);
    }


    /**
     * 创建主面板
     * @return
     */
    private JComponent createRootPanel() {

        // 主布局框架
        ElegantDockingPort mainDockingPanel = new ElegantDockingPort();
        mainDockingPanel.setSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
        ));
        mainDockingPanel.setBorder(DefaultUIConstant.EMPTY_BORDER);


        // 文件树面板
        FileTreePanel fileBrowserPanel = new FileTreePanel(this.listableFileManager);
        fileBrowserPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
        ));
        fileBrowserPanel.setOpaque(false);
        mainDockingPanel.add(fileBrowserPanel, DockingPort.WEST_REGION);


        // 文件表格面板
        FileTablePanel fileListPanel = new FileTablePanel(this.listableFileManager);
        fileListPanel.setPreferredSize(new Dimension(
                DpiUtils.getDpiExtendedSize(
                        DefaultUIConstant.DEFAULT_WIDTH - DefaultUIConstant.DEFAULT_FILE_TREE_PANEL_WITH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_FILE_LIST_PANEL_HEIGHT)
        ));
        mainDockingPanel.add(fileListPanel, DockingPort.EAST_REGION);


        // 跟面板
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setSize(new Dimension(
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_WIDTH),
                DpiUtils.getDpiExtendedSize(DefaultUIConstant.DEFAULT_HEIGHT)
        ));
        rootPanel.add(mainDockingPanel, BorderLayout.CENTER);

        // 创建任务管理器
        TaskManager taskManager = new TaskManager(fileListPanel.getFileTableModel());


        // 创建文件选择监听器
        ListableFileObservable listableFileConnector = new ListableFileObservable(
                this.getMainFrame(),
                fileBrowserPanel,
                fileListPanel,
                listableFileManager,
                fileAlterationMonitor,
                taskManager);
        fileBrowserPanel.setListableFileConnector(listableFileConnector);


        // 创建工具条
        ToolBar toolBar = new ToolBar(JToolBar.HORIZONTAL, listableFileConnector, taskManager);
        JPanel toolBarPanel = new JPanel(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);
        rootPanel.add(toolBarPanel, BorderLayout.NORTH);


        this.fileBrowserPanel = fileBrowserPanel;
        this.fileListPanel = fileListPanel;
        this.mainDockingPanel = mainDockingPanel;
        this.rootPanel = rootPanel;
        this.listableFileConnector = listableFileConnector;
        this.taskManager = taskManager;

        return rootPanel;
    }




}
