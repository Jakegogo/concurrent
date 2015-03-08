package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;

/**
 * 布尔编码器
 * Created by Jake on 2015/2/26.
 */
public class BooleanSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        Boolean bool = (Boolean) object;

        byte booleanVal;
        if (bool.booleanValue()) {
            booleanVal = (byte) 0x01;
        } else {
            booleanVal = (byte) 0x00;
        }

        outputable.putByte((byte) (Types.BOOLEAN | booleanVal));
    }

    @Override
    public void compile(Type type, MethodVisitor mw) {

    }


    private static BooleanSerializer instance = new BooleanSerializer();

    public static BooleanSerializer getInstance() {
        return instance;
    }

}
