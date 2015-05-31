package basesource.gui.contansts;

import basesource.gui.utils.DpiUtils;

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
     * 空边框实例
     */
    Border EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    /**
     * 默认字体
     */
    Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, DpiUtils.getDpiExtendedSize(14));

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
     * 文件表格行填充
     */
    int FILE_TABLE_ROW_PADDING = 5;

    /**
     * 文件表格标题
     */
    String FILE_TABLE_TITLE = "文件预览";

}
