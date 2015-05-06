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
        outputable.putByte((byte)(shortVal >> 8));
        outputable.putByte((byte) shortVal);
    }


    /**
     * 读取整数
     * @param inputable
     * @return
     */
    public static int getInt(final Inputable inputable) {
        byte tmp = inputable.getByte();
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = inputable.getByte()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = inputable.getByte()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = inputable.getByte()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = inputable.getByte()) << 28;
                    if (tmp < 0) {
                        // Discard upper 32 bits.
                        for (int i = 0; i < 5; i++) {
                            if (inputable.getByte() >= 0) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    /**
     * 整数转换成字节
     * @param outputable
     * @param intVal
     */
    public static void putInt(final Outputable outputable, int intVal) {

        while (true) {
            if ((intVal & ~0x7F) == 0) {
                outputable.putByte((byte) intVal);
                return;
            } else {
                outputable.putByte((byte) ((intVal & 0x7F) | 0x80));
                intVal >>>= 7;
            }
        }

    }


    /**
     * 读取长整型
     * @param inputable
     * @return
     */
    public static long getLong(final Inputable inputable) {
        int shift = 0;
        long result = 0;
        while (shift < 64) {
            final byte b = inputable.getByte();
            result |= (long)(b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        return result;
    }



    /**
     * 长整型转换成字节
     * @param outputable
     * @param longVal
     */
    public static void putLong(final Outputable outputable, long longVal) {

        while (true) {
            if ((longVal & ~0x7F) == 0) {
                outputable.putByte((byte) longVal);
                return;
            } else {
                outputable.putByte((byte) ((((int) longVal) & 0x7F) | 0x80));
                longVal >>>= 7;
            }
        }

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
        outputable.putByte((byte)(charVal >> 8));
        outputable.putByte((byte) charVal);
    }


}
