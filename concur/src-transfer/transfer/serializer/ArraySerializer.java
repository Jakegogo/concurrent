package transfer.serializer;

import transfer.Outputable;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

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


    private static ArraySerializer instance = new ArraySerializer();

    public static ArraySerializer getInstance() {
        return instance;
    }

}
