package basesource.convertor;

import basesource.convertor.contansts.Configurations;
import basesource.convertor.ui.MainPanel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 文件浏览器
 * Created by Jake on 2015/5/31.
 */
public class MainApp {

    /** 主面板 */
    private MainPanel mainPanel;

    /** spring ApplicationContext  */
    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
    	// enable anti-aliasing
    	System.setProperty("awt.useSystemAAFontSettings","on");
    	System.setProperty("swing.aatext", "true");

        // 加载文件配置
        Configurations.loadConfigure();

        new MainApp().init(args);
    }

    // 初始化界面
    private void init(String[] args) {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext-basesource.xml");

        mainPanel = new MainPanel(args);
    }

    /**
     * 获取spring容器
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
