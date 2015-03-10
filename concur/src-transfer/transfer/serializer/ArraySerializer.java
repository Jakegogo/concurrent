package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;

/**
 * 数组编码器
 * Created by Jake on 2015/2/25.
 */
public class ArraySerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.ARRAY);

        Object[] array = (Object[]) object;

        for (Object obj : array) {

            Serializer elementSerializer = TransferConfig.getSerializer(obj.getClass());

            elementSerializer.serialze(outputable, obj, referenceMap);
        }

    }

    @Override
    public void compile(Type type, MethodVisitor mw, AsmContext context) {
    	
    }


    private static ArraySerializer instance = new ArraySerializer();

    public static ArraySerializer getInstance() {
        return instance;
    }

}
