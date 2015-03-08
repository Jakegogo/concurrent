package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;

/**
 * NULL编码器
 * Created by Jake on 2015/2/26.
 */
public class NullSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        outputable.putByte(Types.NULL);

    }

    @Override
    public void compile(Type type, MethodVisitor mw, AsmContext context) {

    }


    private static NullSerializer instance = new NullSerializer();

    public static NullSerializer getInstance() {
        return instance;
    }

}
