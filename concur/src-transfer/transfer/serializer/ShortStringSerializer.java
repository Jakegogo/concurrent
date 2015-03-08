package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;

/**
 * 短字符串编码器 最大长度255
 * Created by Jake on 2015/2/26.
 */
public class ShortStringSerializer implements Serializer {


    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.STRING);

        CharSequence charSequence = (CharSequence) object;
        String string = charSequence.toString();

        byte[] bytes = string.getBytes();

        BitUtils.putInt1(outputable, bytes.length);

        outputable.putBytes(bytes);
    }

    @Override
    public void compile(Type type, MethodVisitor mw, AsmContext context) {

    }


    private static ShortStringSerializer instance = new ShortStringSerializer();

    public static ShortStringSerializer getInstance() {
        return instance;
    }

}
