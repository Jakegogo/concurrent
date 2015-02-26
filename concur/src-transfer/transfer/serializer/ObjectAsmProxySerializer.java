package transfer.serializer;

import transfer.Outputable;
import transfer.core.ClassInfo;
import transfer.core.FieldInfo;
import transfer.def.Config;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

/**
 * Asm代理对象编码器
 * Created by Jake on 2015/2/23.
 */
public class ObjectAsmProxySerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NullSerializer.getInstance().serialze(outputable, object, referenceMap);
            return;
        }

        Class<?> clazz = object.getClass().getSuperclass();

        int classId = Config.getClassId(clazz);

        ClassInfo classInfo = Config.getOrCreateClassInfo(clazz);

        outputable.putByte(Types.OBJECT);

        BitUtils.putInt(outputable, classId);

        for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {

            Serializer fieldSerializer = Config.getSerializer(TypeUtils.getRawClass(fieldInfo.getType()));

            Object fieldValue = fieldInfo.getFieldGetter().get(object);

            fieldSerializer.serialze(outputable, fieldValue, referenceMap);

        }

    }


    private static ObjectAsmProxySerializer instance = new ObjectAsmProxySerializer();

    public static ObjectAsmProxySerializer getInstance() {
        return instance;
    }
}
