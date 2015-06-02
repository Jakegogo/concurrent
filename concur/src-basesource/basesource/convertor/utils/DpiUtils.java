package basesource.convertor.utils;

import basesource.convertor.contansts.DefaultUIConstant;

/**
 * Created by Jake on 2015/5/31.
 */
public class DpiUtils {

    /** DPI */
    private static final float dpi = 1.0f;


    /**
     * 获取含DPI配置的大小
     * @param size
     * @return
     */
    public static int getDpiExtendedSize(int size) {
        return (int) Math.floor(size * dpi);
    }


    /**
     * 获取含DPI配置两倍的大小
     * @param size
     * @return
     */
    public static int getDoubleDpiExtendedSize(int size) {
        return (int) Math.floor(size * dpi * 2);
    }


    /**
     * 添加行垂直间距
     * @param height
     * @return
     */
    public static int addLineVerticalPadding(int height) {
        return height + getDoubleDpiExtendedSize(DefaultUIConstant.FILE_TABLE_ROW_DEFAULT_PADDING);
    }


    /**
     * 添加行水平间距
     * @param height
     * @return
     */
    public static int addLineHorizontalPadding(int height) {
        return height + getDoubleDpiExtendedSize(DefaultUIConstant.FILE_TABLE_ROW_DEFAULT_PADDING);
    }

}
