package transfer.deserializer;

import transfer.Inputable;
import transfer.core.ByteMeta;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.exception.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Map解析器
 * <br/>尽量指定泛型类型,可提升解析性能
 * Created by Jake on 2015/2/24.
 */
public class MapDeSerializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.MAP) {
            throw new IllegalTypeException(typeFlag, Types.MAP, type);
        }

        Map<Object, Object> map = createMap(type);

        // 读取数组的大小
        int size = BitUtils.getInt(inputable);

        if (size == 0) {
            return (T) map;
        }

        // 读取元素类型
        byte keyFlag;byte valueFlag;
        Object key;Object value;

        Type keyType = null;
        Type valueType = null;


        if (type instanceof ParameterizedType) {

            keyType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);

            valueType = TypeUtils.getParameterizedType((ParameterizedType) type, 1);
        }


        // 循环解析元素
        for (int i = 0; i < size;i++) {

            keyFlag = inputable.getByte();// 获取key类型

            key = parseElement(inputable, keyType, keyFlag, referenceMap);

            valueFlag = inputable.getByte();// 获取value类型

            value = parseElement(inputable, valueType, valueFlag, referenceMap);

            map.put(key, value);
        }


        return (T) map;
    }


    private Object parseElement(Inputable inputable, Type type, byte byteFlag, IntegerMap referenceMap) {
        Deserializer elementDeserializer = TransferConfig.getDeserializer(type, byteFlag);
        return elementDeserializer.deserialze(inputable, type, byteFlag, referenceMap);
    }


    protected Map<Object, Object> createMap(Type type) {

        if (type == null || type == Map.class || type == Object.class) {
            return new HashMap<Object, Object>();
        }

        if (type == Properties.class) {
            return new Properties();
        }

        if (type == Hashtable.class) {
            return new Hashtable();
        }

        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }

        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }

        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }

        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return createMap(parameterizedType.getRawType());
        }

        Class<?> clazz = (Class<?>) type;
        if (clazz.isInterface()) {
            throw new UnsupportDeserializerTypeException(type);
        }

        try {
            return (Map<Object, Object>) clazz.newInstance();
        } catch (Exception e) {
            throw new UnsupportDeserializerTypeException(type, e);
        }
    }


    public ByteMeta readMeta(Inputable inputable) {

        byte flag = inputable.getByte();
        byte type = TransferConfig.getType(flag);

        if (type != Types.MAP || type != Types.MAP) {
            throw new IllegalTypeException(type, Types.MAP, null);
        }
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);

        ByteMeta byteDataMeta = new ByteMeta();
        byteDataMeta.setComponentSize(size);
        byteDataMeta.setFlag(flag);
        byteDataMeta.setIteratorAble(true);

        return byteDataMeta;
    }


    private static MapDeSerializer instance = new MapDeSerializer();

    public static MapDeSerializer getInstance() {
        return instance;
    }

}
