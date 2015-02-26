package transfer.deserializer;

import transfer.Inputable;
import transfer.def.Config;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map.Entry解析器
 * <br/>尽量指定泛型类型,可提升解析性能
 * Created by Jake on 2015/2/24.
 */
public class EntryDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        Type keyType = null;
        Type valueType = null;

        if (type instanceof ParameterizedType) {

            keyType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);

            valueType = TypeUtils.getParameterizedType((ParameterizedType) type, 1);

        }

        // 读取元素类型
        byte keyFlag = inputable.getByte();
        final Object key = parseElement(inputable, keyType, keyFlag, referenceMap);

        byte valueFlag = inputable.getByte();
        final Object value = parseElement(inputable, valueType, valueFlag, referenceMap);

        return (T) new UnmodificationEntry(key, value);
    }


    private Object parseElement(Inputable inputable, Type keyType, byte keyFlag, IntegerMap referenceMap) {
        Deserializer elementDeserializer = Config.getDeserializer(keyType, keyFlag);
        return elementDeserializer.deserialze(inputable, keyType, keyFlag, referenceMap);
    }


    static class UnmodificationEntry<K, V> implements Map.Entry<K, V> {

        private K key;

        private V value;

        public UnmodificationEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return (key==null   ? 0 : key.hashCode()) ^
                    (value==null ? 0 : value.hashCode());
        }
    }


    private static EntryDeserializer instance = new EntryDeserializer();

    public static EntryDeserializer getInstance() {
        return instance;
    }

}
