package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.core.EnumInfo;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;

/**
 * 枚举编码器
 * Created by Jake on 2015/2/26.
 */
public class EnumSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.ENUM);

        Enum<?> enumVal = (Enum<?>) object;

        EnumInfo enumInfo = (EnumInfo) TransferConfig.getOrCreateClassInfo(enumVal.getDeclaringClass());

        BitUtils.putInt2(outputable, enumInfo.getClassId());

        int enumIndex = enumInfo.toInt(enumVal);

        BitUtils.putInt2(outputable, enumIndex);
    }

    @Override
    public void compile(Type type, MethodVisitor mw) {

    }


    private static EnumSerializer instance = new EnumSerializer();

    public static EnumSerializer getInstance() {
        return instance;
    }

}
