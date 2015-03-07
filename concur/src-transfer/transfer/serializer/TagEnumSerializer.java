package transfer.serializer;

import transfer.Outputable;
import transfer.core.EnumInfo;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

/**
 * 带标签枚举编码器
 * Created by Jake on 2015/2/26.
 */
public class TagEnumSerializer implements Serializer {

    // 标签编码器
    private static final ShortStringSerializer STRING_SERIALIZER = ShortStringSerializer.getInstance();

    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.ENUM);

        Enum<?> enumVal = (Enum<?>) object;

        EnumInfo enumInfo = (EnumInfo) PersistConfig.getOrCreateClassInfo(enumVal.getDeclaringClass());


        BitUtils.putInt2(outputable, enumInfo.getClassId());

        String enumName = enumInfo.toString(enumVal);

        // 添加标签
        STRING_SERIALIZER.serialze(outputable, enumName, referenceMap);

    }


    private static TagEnumSerializer instance = new TagEnumSerializer();

    public static TagEnumSerializer getInstance() {
        return instance;
    }

}
