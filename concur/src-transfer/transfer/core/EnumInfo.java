package transfer.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 枚举信息
 * Created by Jake on 2015/2/25.
 */
public class EnumInfo extends ClassInfo {

    Enum<?>[] values;

    Map<String, Enum<?>> enumMap;

    public static EnumInfo valueOf(Class<? extends Enum> enumClass, int classId) {

        Class<? extends Enum<?>> enumClz = (Class<? extends Enum<?>>) enumClass;

        EnumInfo enumInfo = new EnumInfo();
        enumInfo.clazz = enumClz;

        Enum<?>[] values = enumClz.getEnumConstants();
        enumInfo.values = values;

        Map<String, Enum<?>> enumMap = new HashMap<String, Enum<?>>(values.length + 1);
        for (Enum<?> enumVal : values) {
            enumMap.put(enumVal.name(), enumVal);
        }
        enumInfo.enumMap = enumMap;
        
        enumInfo.classId = classId;

        return enumInfo;
    }


    public int toInt(Enum<?> enumElement) {
        return enumElement.ordinal();
    }


    public Enum<?> toEnum(int index) {
        if (index < 0 || index > values.length) {
            throw new IllegalArgumentException("枚举索引错误:" + clazz + ":" + index);
        }
        return values[index];
    }


    public String toString(Enum<?> enumElement) {
        return enumElement.name();
    }

    public Enum<?> toEnum(String enumName) {
        return enumMap.get(enumName);
    }

    public Enum<?>[] getValues() {
        return values;
    }

}
