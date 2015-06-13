package basesource.convertor.contansts;

import basesource.convertor.utils.DpiUtils;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 常量
 * Created by Jake on 2015/5/31.
 */
public interface DefaultUIConstant {

    /**
     * 默认宽度
     */
    int DEFAULT_WIDTH = 960;

    /**
     * 默认高度
     */
    int DEFAULT_HEIGHT = 680;

    /**
     * 默认文件树宽度
     */
    int DEFAULT_FILE_TREE_PANEL_WITH = 200;

    /**
     * 默认文件列表面板高度
     */
    int DEFAULT_FILE_LIST_PANEL_HEIGHT = 400;

    /**
     * 面板标题栏高度
     */
    int PANEL_TITTLE_BORDER_HEIGHT = 25;

    /**
     * 空边框实例
     */
    Border EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    /**
     * 默认字体大小
     */
    int DEFAULT_FONT_SIZE = 12;
    
    /**
     * 默认字体
     */
    Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, DpiUtils.getDpiExtendedSize(DEFAULT_FONT_SIZE));

    /**
     * 文件表格头文字描述
     */
    String[] FILE_TABLE_HREADER = {
            "",
            "文件名",
            "大小",
            "上次修改时间"
    };

    /**
     * 文件表格行默认填充
     */
    int FILE_TABLE_ROW_DEFAULT_PADDING = 2;

    /**
     * 文件表格标题
     */
    String FILE_TABLE_TITLE = "文件预览";

    /**
     * 选择存储路径按钮
     */
    String SAVE_PATH_BUTTON = "输出";

    /**
     * 开始转换按钮
     */
    String START_CONVERT_BUTTON = "开始";

    /**
     * 暂停转换按钮
     */
    String STOP_CONVERT_BUTTON = "暂停";


    /**
     * 取消转换按钮
     */
    String CANCLE_CONVERT_BUTTON = "取消";

    /**
     * 打开目标文件夹按钮
     */
    String OPEN_CONVERT_BUTTON = "查看";

    /**
     * 选择输出路径提示
     */
    String ACCECPT_FILE_LIMIT_TIP = "文件夹..";

    /**
     * 文件监视时间间隔
     */
    long FILE_MONITOR_INTERVAL = 3 * 1000;
    
    /**
     * 文件名编码
     */
    String FILE_NAME_ENCODE = "GBK";

    /**
     * 文件树目录面板标题
     */
    String FILE_TREE_PANEL_TITTLE = "输入文件夹";

    /**
     * 表格进度条高度
     */
    int TABLE_ROW_PROGRESS_BAR_HEIGHT = -1;


    /**
     * 表格进度条颜色
     */
    Color TABLE_ROW_PROGRESS_BAR_COLOR1 = new Color(246, 246, 246);

    /**
     * 表格进度条颜色
     */
    Color TABLE_ROW_PROGRESS_BAR_COLOR2 = new Color(45, 255, 2);

    /**
     * 选择代码路径提示
     */
    String CODE_INPUT_PATH_TIP = "代码路径";


    /**
     * UIManager中UI字体相关的key
     */
    String[] DEFAULT_FONT_COMPONENT = new String[]{
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

}
