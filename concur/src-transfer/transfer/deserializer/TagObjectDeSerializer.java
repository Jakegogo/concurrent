package transfer.deserializer;

import transfer.Inputable;
import transfer.core.ClassInfo;
import transfer.core.FieldInfo;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.exception.IllegalClassTypeException;
import transfer.exception.IllegalTypeException;
import transfer.exception.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 带标签的对象解析器
 * Created by Jake on 2015/2/23.
 */
public class TagObjectDeSerializer implements Deserializer {

    /**
     * 属性名解析器
     */
    private static final StringDeserializer STRING_DESERIALIZER = StringDeserializer.getInstance();


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = PersistConfig.getType(flag);

        if (typeFlag != Types.OBJECT) {
            throw new IllegalTypeException(typeFlag, Types.OBJECT, type);
        }

        // 读取对象类型
        int classId = BitUtils.getInt2(inputable);

        Class<?> rawClass;

        if (type == null || type == Object.class) {

            rawClass = PersistConfig.getClass(classId);
        } else {

            rawClass = TypeUtils.getRawClass(type);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        ClassInfo classInfo = PersistConfig.getOrCreateClassInfo(rawClass);

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
        String fieldName;
        Object fieldValue;
        FieldInfo fieldInfo;
        Deserializer fieldDeserializer;

        // 读取属性数量
        int fieldNum = BitUtils.getInt2(inputable);

        for (int i = 0;i < fieldNum;i++) {

            fieldName = STRING_DESERIALIZER.deserialze(inputable, String.class, inputable.getByte(), referenceMap);

            fieldInfo = classInfo.getFieldInfo(fieldName);
            if (fieldInfo == null) {// 略过不存在的属性
                continue;
            }

            byte fieldFlag = inputable.getByte();

            fieldType = fieldInfo.getType();

            fieldDeserializer = PersistConfig.getDeserializer(fieldType, fieldFlag);

            fieldValue = fieldDeserializer.deserialze(inputable, fieldType, fieldFlag, referenceMap);

            fieldInfo.setField(object, fieldValue);

        }

        return (T) object;
    }


    private static TagObjectDeSerializer instance = new TagObjectDeSerializer();

    public static TagObjectDeSerializer getInstance() {
        return instance;
    }

}
