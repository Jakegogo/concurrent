package transfer.deserializer;

import transfer.Inputable;
import transfer.core.ClassInfo;
import transfer.core.FieldInfo;
import transfer.def.Config;
import transfer.def.Types;
import transfer.exception.IllegalClassTypeException;
import transfer.exception.IllegalTypeException;
import transfer.exception.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 对象解析器
 * Created by Jake on 2015/2/23.
 */
public class ObjectDeSerializer implements Deserializer {



    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = Config.getType(flag);

        if (typeFlag != Types.OBJECT) {
            throw new IllegalTypeException(typeFlag, Types.OBJECT, type);
        }

        // 读取对象类型
        int classId = BitUtils.getInt(inputable);

        Class<?> rawClass;

        if (type == null || type == Object.class) {

            rawClass = Config.getClass(classId);
        } else {

            rawClass = TypeUtils.getRawClass(type);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        ClassInfo classInfo = Config.getOrCreateClassInfo(rawClass);

        if (classInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (classId != classInfo.getClassId()) {
            throw new IllegalClassTypeException(classId, type);
        }

        Object object;
        try {
            object = rawClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("create instane error, class " + rawClass.getName());
        }


        Type fieldType;
        Object fieldValue;
        Deserializer fieldDeserializer;

        for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {

            byte fieldFlag = inputable.getByte();

            fieldType = fieldInfo.getType();

            fieldDeserializer = Config.getDeserializer(fieldType, fieldFlag);

            fieldValue = fieldDeserializer.deserialze(inputable, fieldType, fieldFlag, referenceMap);

            fieldInfo.setField(object, fieldValue);

        }

        return (T) object;
    }


    private static ObjectDeSerializer instance = new ObjectDeSerializer();

    public static ObjectDeSerializer getInstance() {
        return instance;
    }

}
