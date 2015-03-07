package transfer.serializer;

import transfer.Outputable;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.util.Map;

/**
 * Map编码器
 * Created by Jake on 2015/2/25.
 */
public class MapSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.MAP);

        Map<?, ?> map = (Map<?, ?>) object;

        // 设置Map的大小
        BitUtils.putInt(outputable, map.size());

        Object key, value;
        for (Map.Entry entry : map.entrySet()) {

            key = entry.getKey();

            Serializer keySerializer = TransferConfig.getSerializer(key.getClass());

            keySerializer.serialze(outputable, key, referenceMap);


            value = entry.getValue();

            Serializer valueSerializer = TransferConfig.getSerializer(value.getClass());

            valueSerializer.serialze(outputable, value, referenceMap);
        }

    }


    private static MapSerializer instance = new MapSerializer();

    public static MapSerializer getInstance() {
        return instance;
    }

}
