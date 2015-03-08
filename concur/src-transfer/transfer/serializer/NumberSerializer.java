package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;
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

    // 0000 1000
    public static final byte FLAG_NEGATIVE = (byte) 0x08;

    // 0000 0000
    public static final byte FLAG_NOT_NEGATIVE = (byte) 0x00;

    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
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

        } else if (object instanceof Float || object instanceof Double
                || object instanceof BigDecimal) {

            DecimalSerializer.getInstance().serialze(outputable, object, referenceMap);
        }

    }

    @Override
    public void compile(Type type, MethodVisitor mw, AsmContext context) {

    }


    private void putIntVal(Outputable outputable, Number number) {

        int value = number.intValue();

        byte sign = FLAG_NOT_NEGATIVE;
        if (value < 0) {
            value = -value;
            sign = FLAG_NEGATIVE;
        }

        if (value < FLAG_0X80) {

            outputable.putByte((byte) (Types.NUMBER | sign | TransferConfig.INT1), (byte) value);
        } else {

            byte[] bytes = new byte[4];

            int i = 4;
            while (value > 0) {
                bytes[--i] = (byte) value;
                value >>= 8;
            }

            outputable.putByte((byte) (Types.NUMBER | sign | (3 - i)));
            outputable.putBytes(bytes, i, 4 - i);
        }

   }


    private void putLongVal(Outputable outputable, Number number) {

        long value = number.longValue();

        byte sign = FLAG_NOT_NEGATIVE;
        if (value < 0) {
            value = -value;
            sign = FLAG_NEGATIVE;
        }

        if (value < FLAG_0X80) {

            outputable.putByte((byte) (Types.NUMBER | sign | TransferConfig.INT1), (byte) value);
        } else {

            byte[] bytes = new byte[8];

            int i = 8;
            while (value > 0) {
                bytes[--i] = (byte) value;
                value >>= 8;
            }

            outputable.putByte((byte) (Types.NUMBER | sign | (7 - i)));
            outputable.putBytes(bytes, i, 8 - i);
        }

    }


    private static NumberSerializer instance = new NumberSerializer();

    public static NumberSerializer getInstance() {
        return instance;
    }

}
