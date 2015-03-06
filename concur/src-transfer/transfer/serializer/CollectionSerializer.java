package transfer.serializer;

import transfer.Outputable;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.util.Collection;

/**
 * 集合编码器
 * Created by Jake on 2015/2/25.
 */
public class CollectionSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.COLLECTION);

        Collection collection = (Collection) object;

        // 设置集合大小
        BitUtils.putInt(outputable, collection.size());

        for (Object element : collection) {

            Serializer elementSerializer = TransferConfig.getSerializer(element.getClass());

            elementSerializer.serialze(outputable, element, referenceMap);

        }

    }


    private static CollectionSerializer instance = new CollectionSerializer();

    public static CollectionSerializer getInstance() {
        return instance;
    }

}
