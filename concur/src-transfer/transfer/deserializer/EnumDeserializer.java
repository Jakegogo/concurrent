package transfer.deserializer;

import transfer.Inputable;
import transfer.core.EnumInfo;
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
 * 枚举解析器
 * Created by Jake on 2015/2/25.
 */
public class EnumDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = Config.getType(flag);

        if (typeFlag != Types.ENUM) {
            throw new IllegalTypeException(typeFlag, Types.ENUM, type);
        }

        // 读取枚举类型
        int enumType = BitUtils.getInt2(inputable);

        Class<?> rawClass;

        if (type == null || type == Object.class || type == Enum.class) {

            rawClass = Config.getClass(enumType);

        } else {

            rawClass = TypeUtils.getRawClass(type);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        EnumInfo enumInfo = (EnumInfo) Config.getOrCreateClassInfo(rawClass);

        if (enumInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (enumType != enumInfo.getClassId()) {
            throw new IllegalClassTypeException(enumType, type);
        }

        // 读取枚举索引
        int enumIndex = BitUtils.getInt2(inputable);

        return (T) enumInfo.toEnum(enumIndex);
    }


    private static EnumDeserializer instance = new EnumDeserializer();

    public static EnumDeserializer getInstance() {
        return instance;
    }

}
