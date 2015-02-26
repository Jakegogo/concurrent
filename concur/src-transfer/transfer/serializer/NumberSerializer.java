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


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NullSerializer.getInstance().serialze(outputable, object, referenceMap);
            return;
        }

        Number number = (Number) object;

        if (object instanceof Integer
                || object instanceof Short
                || object instanceof Byte
                || object instanceof AtomicInteger) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT32));
            BitUtils.putInt(outputable, number.intValue());
        } else if(object instanceof Long
                || object instanceof AtomicLong
                || object instanceof BigInteger) {
            outputable.putByte((byte) (Types.NUMBER | Config.INT64));
            BitUtils.putLong(outputable, number.longValue());
        } else if(object instanceof Float) {
            outputable.putByte((byte) (Types.NUMBER | Config.FLOAT));
            BitUtils.putFloat(outputable, number.floatValue());
        } else if(object instanceof Double
                || object instanceof BigDecimal) {
            outputable.putByte((byte) (Types.NUMBER | Config.DOUBLE));
            BitUtils.putDouble(outputable, number.doubleValue());
        }

    }


    private static NumberSerializer instance = new NumberSerializer();

    public static NumberSerializer getInstance() {
        return instance;
    }

}
