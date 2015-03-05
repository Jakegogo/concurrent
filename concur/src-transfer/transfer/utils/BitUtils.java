package transfer.utils;

import transfer.Inputable;
import transfer.Outputable;

/**
 * 字节工具类
 * Created by Jake on 2015/2/23.
 */
public class BitUtils {


    /**
     * 读取整数
     * @param inputable
     * @return
     */
    public static short getShort(final Inputable inputable) {
        return (short)((inputable.getByte() << 8) | (inputable.getByte() & 0xff));
    }


    /**
     * 短整数转换成字节
     * @param outputable
     * @param shortVal
     */
    public static void putShort(final Outputable outputable, final short shortVal) {
        outputable.putByte((byte)(shortVal >> 8), (byte)(shortVal >> 0));
    }


    /**
     * 读取整数
     * @param inputable
     * @return
     */
    public static int getInt(final Inputable inputable) {
        byte[] longBytes = new byte[4];
        inputable.getBytes(longBytes);
        return ((((longBytes[0] & 0xff) << 24) |
                ((longBytes[1] & 0xff) << 16) |
                ((longBytes[2] & 0xff) <<  8) |
                ((longBytes[3] & 0xff) <<  0)));
    }


    /**
     * 读取整数
     * @param inputable
     * @return
     */
    public static int getInt1(final Inputable inputable) {
        return inputable.getByte() & 0xff;
    }


    /**
     * 读取整数
     * @param inputable
     * @return
     */
    public static int getInt2(final Inputable inputable) {
        return ((inputable.getByte() & 0xff) <<  8) |
                ((inputable.getByte() & 0xff) <<  0);
    }

    /**
     * 读取整数
     * @param inputable
     * @return
     */
    public static int getInt3(final Inputable inputable) {
        byte[] longBytes = new byte[3];
        inputable.getBytes(longBytes);
        return ((((longBytes[0] & 0xff) << 16) |
                ((longBytes[1] & 0xff) <<  8) |
                ((longBytes[2] & 0xff) <<  0)));
    }


    /**
     * 整数转换成字节
     * @param outputable
     * @param intVal
     */
    public static void putInt(final Outputable outputable, final int intVal) {
        outputable.putByte((byte)(intVal >> 24), (byte)(intVal >> 16), (byte)(intVal >> 8), (byte)(intVal >> 0));
    }


    /**
     * 整数转换成字节
     * @param outputable
     * @param intVal
     */
    public static void putInt2(final Outputable outputable, final int intVal) {
        outputable.putByte((byte)(intVal >> 8), (byte)(intVal >> 0));
    }


    /**
     * 读取长整型
     * @param inputable
     * @return
     */
    public static long getLong(final Inputable inputable) {
        byte[] longBytes = new byte[8];
        inputable.getBytes(longBytes);
        return ((((long) longBytes[0] & 0xff) << 56) |
                (((long) longBytes[1] & 0xff) << 48) |
                (((long) longBytes[2] & 0xff) << 40) |
                (((long) longBytes[3] & 0xff) << 32) |
                (((long) longBytes[4] & 0xff) << 24) |
                (((long) longBytes[5] & 0xff) << 16) |
                (((long) longBytes[6] & 0xff) <<  8) |
                (((long) longBytes[7] & 0xff) <<  0));
    }


    /**
     * 读取长整型
     * @param inputable
     * @return
     */
    public static long getLong5(final Inputable inputable) {
        byte[] longBytes = new byte[5];
        inputable.getBytes(longBytes);
        return ((((long) longBytes[0] & 0xff) << 32) |
                (((long) longBytes[1] & 0xff) << 24) |
                (((long) longBytes[2] & 0xff) << 16) |
                (((long) longBytes[3] & 0xff) <<  8) |
                (((long) longBytes[4] & 0xff) <<  0));
    }


    /**
     * 读取长整型
     * @param inputable
     * @return
     */
    public static long getLong6(final Inputable inputable) {
        byte[] longBytes = new byte[6];
        inputable.getBytes(longBytes);
        return ((((long) longBytes[0] & 0xff) << 40) |
                (((long) longBytes[1] & 0xff) << 32) |
                (((long) longBytes[2] & 0xff) << 24) |
                (((long) longBytes[3] & 0xff) << 16) |
                (((long) longBytes[4] & 0xff) <<  8) |
                (((long) longBytes[5] & 0xff) <<  0));
    }


    /**
     * 读取长整型
     * @param inputable
     * @return
     */
    public static long getLong7(final Inputable inputable) {
        byte[] longBytes = new byte[7];
        inputable.getBytes(longBytes);
        return ((((long) longBytes[0] & 0xff) << 48) |
                (((long) longBytes[1] & 0xff) << 40) |
                (((long) longBytes[2] & 0xff) << 32) |
                (((long) longBytes[3] & 0xff) << 24) |
                (((long) longBytes[4] & 0xff) << 16) |
                (((long) longBytes[5] & 0xff) <<  8) |
                (((long) longBytes[6] & 0xff) <<  0));
    }



    /**
     * 长整型转换成字节
     * @param outputable
     * @param longVal
     */
    public static void putLong(final Outputable outputable, final long longVal) {
        outputable.putByte((byte)(longVal >> 56), (byte)(longVal >> 48), (byte)(longVal >> 40), (byte)(longVal >> 32),
                (byte)(longVal >> 24), (byte)(longVal >> 16), (byte)(longVal >> 8), (byte)(longVal >> 0));
    }


    /**
     * 读取浮点型数据
     * @param inputable
     * @return
     */
    public static float getFloat(final Inputable inputable) {
        return Float.intBitsToFloat(getInt(inputable));
    }


    /**
     * 浮点型转换成字节
     * @param outputable
     * @param floatVal
     */
    public static void putFloat(final Outputable outputable, final float floatVal) {
        putInt(outputable, Float.floatToRawIntBits(floatVal));
    }


    /**
     * 读取Double类型数据
     * @param inputable
     * @return
     */
    public static double getDouble(final Inputable inputable) {
        return Double.longBitsToDouble(getLong(inputable));
    }


    /**
     * 将Double型转换成字节
     * @param outputable
     * @param doubleVal
     */
    public static void putDouble(final Outputable outputable, final double doubleVal) {
        putLong(outputable, Double.doubleToRawLongBits(doubleVal));
    }


    /**
     * 转换成Char
     * @param inputable
     * @return
     */
    public static char getChar(final Inputable inputable) {
        return (char)((inputable.getByte() << 8) | (inputable.getByte() & 0xff));
    }


    /**
     * Char转换成字节
     * @param outputable
     * @param charVal
     */
    public static void putChar(final Outputable outputable, final char charVal) {
        outputable.putByte((byte)(charVal >> 8), (byte)(charVal >> 0));
    }


}
