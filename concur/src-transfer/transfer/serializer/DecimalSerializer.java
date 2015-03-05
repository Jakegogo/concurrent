package transfer.serializer;

import transfer.Outputable;
import transfer.def.Config;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.math.BigDecimal;

/**
 * 浮点输编码器
 * Created by Jake on 2015/2/26.
 */
public class DecimalSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            Config.NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        Number number = (Number) object;

        if (object instanceof Float) {

            outputable.putByte((byte) (Types.DECIMAL | Config.FLOAT));
            BitUtils.putInt(outputable, Float.floatToRawIntBits(number.floatValue()));

        } else if(object instanceof Double
                || object instanceof BigDecimal) {

            outputable.putByte((byte) (Types.DECIMAL | Config.DOUBLE));
            BitUtils.putLong(outputable, Double.doubleToRawLongBits(number.doubleValue()));

        }

    }


    private static DecimalSerializer instance = new DecimalSerializer();

    public static DecimalSerializer getInstance() {
        return instance;
    }

}
