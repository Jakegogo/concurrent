package utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.apache.commons.lang.ArrayUtils;


/**
 * Protostuff编解码工具
 */
public class ProtostuffUtils {

    /**
     * 字节数组转换成对象
     * @param data 字节数组
     * @param type 类型
     * @param <T>
     * @return
     */
    public static <T> T bytes2Object(byte[] data, Class<T> type) {

        Schema<T> schema = RuntimeSchema.getSchema(type);
        T obj = schema.newMessage();

        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }


    /**
     * 对象转换成字节数组
     * @param obj 目标对象
     * @param <T>
     * @return
     */
    public static <T> byte[] object2Bytes(T obj) {
        if (obj == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        Class<T> clazz = (Class<T>) obj.getClass();
        Schema<T> schema = RuntimeSchema.getSchema(clazz);

        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
    }

}