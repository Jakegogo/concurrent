package transfer;

import transfer.core.ByteDataMeta;
import transfer.def.Config;
import transfer.def.Types;
import transfer.deserializer.CollectionDeSerializer;
import transfer.deserializer.Deserializer;
import transfer.deserializer.MapDeSerializer;
import transfer.serializer.NullSerializer;
import transfer.serializer.Serializer;
import transfer.utils.IdentityHashMap;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 字节传输协议
 * <br/>定义传输类的属性按定义属性的先后顺序传输
 * <br/>定义传输类必须调用Config#registerClass(java.lang.Class<?>, int)进行注册
 * Created by Jake on 2015/2/22.
 */
public class Transfer {


    // notes:
    // 可迭代解码,调用Iterator.next()方法时进行集合元素的解码
    // Inputable、Outputable适配网络框架的readBuffer、writeBuffer
    // TypeReference指定泛型类型将预编译,可提升解析速度
    //


    /**
     * 解码
     * @param inputable
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T decode(Inputable inputable, Class<T> clazz) {
        byte flag = inputable.getByte();
        Deserializer deserializer = Config.getDeserializer((Type) clazz, flag);
        return deserializer.deserialze(inputable, clazz, flag, new IntegerMap(16));
    }


    /**
     * 解码
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T decode(byte[] bytes, Class<T> clazz) {
        Inputable inputable = new ByteArray(bytes);
        byte flag = inputable.getByte();
        Deserializer deserializer = Config.getDeserializer((Type) clazz, flag);
        return deserializer.deserialze(inputable, clazz, flag, new IntegerMap(16));
    }


    /**
     * 解码
     * @param inputable
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T decode(Inputable inputable, TypeReference<T> typeReference) {
        byte flag = inputable.getByte();
        Deserializer deserializer = Config.getDeserializer(typeReference.getType(), flag);
        return deserializer.deserialze(inputable, typeReference.getType(), flag, new IntegerMap(16));
    }



    /**
     * 解码
     * @param bytes
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T decode(byte[] bytes, TypeReference<T> typeReference) {
        Inputable inputable = new ByteArray(bytes);
        byte flag = inputable.getByte();
        Deserializer deserializer = Config.getDeserializer(typeReference.getType(), flag);
        return deserializer.deserialze(inputable, typeReference.getType(), flag, new IntegerMap(16));
    }



    /**
     * 迭代解码
     * @param inputable
     * @param typeReference
     * @param <T> 集合类型类型
     * @return
     */
    public static <T extends Collection<E>, E> Iterator<E> iterator(final Inputable inputable, TypeReference<T> typeReference) {
        // 读取消息头
        final ByteDataMeta byteDataMeta = CollectionDeSerializer.getInstance().readMeta(inputable);
        // 不可以迭代
        if (byteDataMeta == null || !byteDataMeta.isIteratorAble()) {
            throw new UnsupportedOperationException();
        }

        final Type componentType = new TypeReference<E>().getType();// 取出元素类型
        final Deserializer componentDeserializer = Config.getDeserializer(componentType, byteDataMeta.getFlag());// 元素解析器

        final IntegerMap referenceMap = new IntegerMap(16);
        return new Iterator<E>() {

            private int curIndex = 0;

            private int size = byteDataMeta.getComponentSize();

            @Override
            public boolean hasNext() {
                return curIndex < size;
            }

            @Override
            public E next() {
                curIndex ++;
                return componentDeserializer.deserialze(inputable, componentType, inputable.getByte(), referenceMap);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    /**
     * 迭代解码
     * @param bytes
     * @param typeReference
     * @param <T> 集合类型类型
     * @return
     */
    public static <T extends Collection<E>, E> Iterator<E> iterator(byte[] bytes, TypeReference<T> typeReference) {
        return iterator(new ByteArray(bytes), typeReference);
    }


    /**
     * 迭代解码
     * @param inputable
     * @param typeReference
     * @param <T> Map类型
     * @return
     */
    public static <T extends Map<K, V>, K, V> Iterator<Map.Entry<K, V>> iteratorMap(final Inputable inputable, TypeReference<T> typeReference) {
        // 读取消息头
        final ByteDataMeta byteDataMeta = MapDeSerializer.getInstance().readMeta(inputable);
        // 不可以迭代
        if (byteDataMeta == null || !byteDataMeta.isIteratorAble()) {
            throw new UnsupportedOperationException();
        }

        final Type componentType = new TypeReference<Map.Entry<K, V>>().getType();// 取出元素类型
        final Deserializer entryDeserializer = Config.getDeserializer(componentType, byteDataMeta.getFlag());// 元素解析器

        final IntegerMap referenceMap = new IntegerMap(16);
        return new Iterator<Map.Entry<K, V>>() {

            private int curIndex = 0;

            private int size = byteDataMeta.getComponentSize();

            @Override
            public boolean hasNext() {
                return curIndex < size;
            }

            @Override
            public Map.Entry<K, V> next() {
                curIndex ++;
                return entryDeserializer.deserialze(inputable, (Type) Map.Entry.class, Types.UNKOWN, referenceMap);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    /**
     * 迭代解码
     * @param bytes
     * @param typeReference
     * @param <T> Map类型
     * @return
     */
    public static <T extends Map<K, V>, K, V> Iterator<Map.Entry<K, V>> iteratorMap(byte[] bytes, TypeReference<T> typeReference) {
        return iteratorMap(new ByteArray(bytes), typeReference);
    }


    /**
     * 编码
     * @param outputable
     * @param object
     */
    public static void encode(Outputable outputable, Object object) {

        if (object == null) {
            NullSerializer.getInstance().serialze(outputable, object, null);
            return;
        }

        Serializer serializer = Config.getSerializer(object.getClass());
        serializer.serialze(outputable, object, new IdentityHashMap(16));
    }


    /**
     * 编码
     * @param object
     */
    public static ByteArray encode(Object object) {

        if (object == null) {
            ByteBuffer buffer = new ByteBuffer(1);
            NullSerializer.getInstance().serialze(buffer, object, null);
            return buffer.getByteArray();
        }

        Serializer serializer = Config.getSerializer(object.getClass());
        ByteBuffer buffer = new ByteBuffer(256);

        serializer.serialze(buffer, object, new IdentityHashMap(16));
        return buffer.getByteArray();
    }


}
