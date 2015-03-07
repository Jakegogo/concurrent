package transfer.deserializer;

import transfer.Inputable;
import transfer.core.ByteDataMeta;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 数组解析器
 * <br/>尽量指定元素类型,可提升解析性能
 * Created by Jake on 2015/2/23.
 */
public class ArrayDeSerializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.ARRAY && typeFlag != Types.COLLECTION) {
            throw new IllegalTypeException(typeFlag, Types.ARRAY, type);
        }

        // 读取数组的大小
        int size = BitUtils.getInt(inputable);
        if (size == 0) {
            return (T) new Object[0];
        }


        Type itemType = null;
        if (type instanceof Class<?> && ((Class<?>)type).isArray()) {
            itemType = ((Class<?>)type).getComponentType();
        } else if (type instanceof ParameterizedType) {
            itemType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        }


        Object[] array = (Object[]) Array.newInstance(TypeUtils.getRawClass(itemType), size);


        Deserializer defaultComponentDeserializer = null;
        if (itemType != null && itemType != Object.class) {
            defaultComponentDeserializer = TransferConfig.getDeserializer(itemType);// 元素解析器
        }


        // 循环解析元素
        Object component;
        if (defaultComponentDeserializer == null) {
            for (int i = 0; i < size;i++) {
                byte elementFlag = inputable.getByte();
                Deserializer componentDeserializer = TransferConfig.getDeserializer(itemType, elementFlag);// 元素解析器
                component =  componentDeserializer.deserialze(inputable, itemType, elementFlag, referenceMap);
                array[i] = component;
            }
        } else {
            for (int i = 0; i < size;i++) {
                component = defaultComponentDeserializer.deserialze(inputable, itemType, inputable.getByte(), referenceMap);
                array[i] = component;
            }
        }

        return (T) array;
    }


    public ByteDataMeta readMeta(Inputable inputable) {

        byte flag = inputable.getByte();
        byte type = TransferConfig.getType(flag);

        if (type != Types.ARRAY && type != Types.COLLECTION) {
            throw new IllegalTypeException(type, Types.ARRAY, null);
        }
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);

        ByteDataMeta byteDataMeta = new ByteDataMeta();
        byteDataMeta.setComponentSize(size);
        byteDataMeta.setFlag(flag);
        byteDataMeta.setIteratorAble(true);

        return byteDataMeta;
    }


    private static ArrayDeSerializer instance = new ArrayDeSerializer();

    public static ArrayDeSerializer getInstance() {
        return instance;
    }

}
