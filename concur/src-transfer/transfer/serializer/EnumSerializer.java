package transfer.serializer;

import transfer.Outputable;
import transfer.core.EnumInfo;
import transfer.def.Config;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

/**
 * 枚举解析器
 * Created by Jake on 2015/2/26.
 */
public class EnumSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NullSerializer.getInstance().serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.ENUM);

        Enum<?> enumVal = (Enum<?>) object;

        int enumId = Config.getClassId(enumVal.getDeclaringClass());

        BitUtils.putInt(outputable, enumId);

        EnumInfo enumInfo = (EnumInfo) Config.getOrCreateClassInfo(enumVal.getDeclaringClass());

        int enumIndex = enumInfo.toInt(enumVal);

        BitUtils.putInt(outputable, enumIndex);
    }


    private static EnumSerializer instance = new EnumSerializer();

    public static EnumSerializer getInstance() {
        return instance;
    }

}
