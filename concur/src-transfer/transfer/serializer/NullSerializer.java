package transfer.serializer;

import transfer.Outputable;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

/**
 * NULL编码器
 * Created by Jake on 2015/2/26.
 */
public class NullSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        outputable.putByte(Types.NULL);

    }


    private static NullSerializer instance = new NullSerializer();

    public static NullSerializer getInstance() {
        return instance;
    }

}
