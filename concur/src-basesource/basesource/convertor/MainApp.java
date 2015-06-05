package basesource.convertor;

import basesource.convertor.ui.MainPanel;

/**
 * 文件浏览器
 * Created by Jake on 2015/5/31.
 */
public class MainApp {

    /** 主面板 */
    private MainPanel mainPanel;

    public static void main(String[] args) {
    	// enable anti-aliasing
    	System.setProperty("awt.useSystemAAFontSettings","on");
    	System.setProperty("swing.aatext", "true");
    	
        new MainApp().init(args);
    }

    // 初始化界面
    private void init(String[] args) {
        mainPanel = new MainPanel(args);
    }


}
