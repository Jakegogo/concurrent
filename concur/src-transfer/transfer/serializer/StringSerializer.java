package transfer.serializer;

import transfer.Outputable;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

/**
 * 字符串编码器
 * Created by Jake on 2015/2/26.
 */
public class StringSerializer implements Serializer {


    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.STRING);

        CharSequence charSequence = (CharSequence) object;
        String string = charSequence.toString();

        byte[] bytes = string.getBytes();

        BitUtils.putInt(outputable, bytes.length);

        outputable.putBytes(bytes);
    }


    private static StringSerializer instance = new StringSerializer();

    public static StringSerializer getInstance() {
        return instance;
    }

}
