package basesource.gui.utils;

/**
 * Created by Jake on 2015/5/31.
 */
public class DpiUtils {

    /** DPI */
    private static final float dpi = 1.2f;


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

}
