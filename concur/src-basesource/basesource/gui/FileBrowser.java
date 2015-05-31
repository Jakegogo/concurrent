package basesource.gui;

/**
 * 文件浏览器
 * Created by Jake on 2015/5/31.
 */
public class FileBrowser {

    /** 主面板 */
    private MainPanel mainPanel;

    public static void main(String[] args) {
        new FileBrowser().init(args);
    }

    // 初始化界面
    private void init(String[] args) {
        mainPanel = new MainPanel(args);
    }


}
