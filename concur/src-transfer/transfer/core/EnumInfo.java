package transfer.core;

/**
 * 枚举信息
 * Created by Jake on 2015/2/25.
 */
public class EnumInfo extends ClassInfo {

    Enum<?>[] values;


    public static EnumInfo valueOf(Class<? extends Enum> enumClass) {

        Class<? extends Enum<?>> enumClz = (Class<? extends Enum<?>>) enumClass;

        EnumInfo enumInfo = new EnumInfo();
        enumInfo.clazz = enumClz;

        Enum<?>[] values = enumClz.getEnumConstants();
        enumInfo.values = values;

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

}
