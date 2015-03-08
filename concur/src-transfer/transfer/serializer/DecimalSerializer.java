package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * 浮点数编码器
 * Created by Jake on 2015/2/26.
 */
public class DecimalSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        Number number = (Number) object;

        if (object instanceof Float) {

            outputable.putByte((byte) (Types.DECIMAL | TransferConfig.FLOAT));
            BitUtils.putInt(outputable, Float.floatToRawIntBits(number.floatValue()));

        } else if(object instanceof Double
                || object instanceof BigDecimal) {

            outputable.putByte((byte) (Types.DECIMAL | TransferConfig.DOUBLE));
            BitUtils.putLong(outputable, Double.doubleToRawLongBits(number.doubleValue()));

        }

    }

    @Override
    public void compile(Type type, MethodVisitor mw, AsmContext context) {

    }


    private static DecimalSerializer instance = new DecimalSerializer();

    public static DecimalSerializer getInstance() {
        return instance;
    }

}
