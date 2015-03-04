package transfer.serializer;

import transfer.Outputable;
import transfer.def.Config;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数字编码器
 * Created by Jake on 2015/2/26.
 */
public class NumberSerializer implements Serializer {

    // 1000 0000
    public static final int FLAG_0X80 = 0x80;

    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            Config.NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        Number number = (Number) object;

        if (object instanceof Integer
                || object instanceof Short
                || object instanceof Byte
                || object instanceof AtomicInteger) {

            this.putIntVal(outputable, number);

        } else if(object instanceof Long
                || object instanceof AtomicLong
                || object instanceof BigInteger) {

            this.putLongVal(outputable, number);

        } else if(object instanceof Float) {

            this.putFloatVal(outputable, number);

        } else if(object instanceof Double
                || object instanceof BigDecimal) {

            this.putDoubleVal(outputable, number);

        }

    }


    private void putIntVal(Outputable outputable, Number number) {

        int value = number.intValue();
        if (value < FLAG_0X80) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT321), (byte) value);
        } else if ((value >>> 24) > 0) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT324), (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
        } else if((value >>> 16) > 0) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT323), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
        } else if((value >>> 8) > 0) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT322), (byte)(value >> 8), (byte)(value >> 0));
        } else {
            outputable.putByte((byte) (Types.NUMBER | Config.INT321), (byte)(value >> 0));
        }

    }


    private void putLongVal(Outputable outputable, Number number) {

        long value = number.longValue();
        if (value < FLAG_0X80) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT641), (byte)(value >> 0));
        } else if(value <= Integer.MAX_VALUE) {
            if ((value >>> 24) > 0) {
                outputable.putByte((byte) (Types.NUMBER | Config.INT644), (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
            } else if ((value >>> 16) > 0) {
                outputable.putByte((byte) (Types.NUMBER | Config.INT643), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
            } else if ((value >>> 8) > 0) {
                outputable.putByte((byte) (Types.NUMBER | Config.INT642), (byte)(value >> 8), (byte)(value >> 0));
            } else {
                outputable.putByte((byte) (Types.NUMBER | Config.INT641), (byte)(value >> 0));
            }
        } else if(value <= 0x00FFFFFFFFFFFFFFL) {
            if ((value >>> 56) > 0) {
                outputable.putByte((byte) (Types.NUMBER | Config.INT648), (byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32),
                        (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
            } else if ((value >>> 48) > 0) {
                outputable.putByte((byte) (Types.NUMBER | Config.INT647), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32),
                        (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
            } else if ((value >>> 40) > 0) {
                outputable.putByte((byte) (Types.NUMBER | Config.INT646), (byte)(value >> 40), (byte)(value >> 32),
                        (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
            } else {
                outputable.putByte((byte) (Types.NUMBER | Config.INT645), (byte)(value >> 32),
                        (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)(value >> 0));
            }
        }

    }


    private void putFloatVal(Outputable outputable, Number number) {
        outputable.putByte((byte) (Types.NUMBER | Config.FLOAT));
        BitUtils.putInt(outputable, Float.floatToRawIntBits(number.floatValue()));
    }


    private void putDoubleVal(Outputable outputable, Number number) {
        outputable.putByte((byte) (Types.NUMBER | Config.DOUBLE));
        BitUtils.putLong(outputable, Double.doubleToRawLongBits(number.doubleValue()));
    }


    private static NumberSerializer instance = new NumberSerializer();

    public static NumberSerializer getInstance() {
        return instance;
    }

}
