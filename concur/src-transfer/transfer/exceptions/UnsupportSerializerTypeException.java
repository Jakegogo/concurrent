package transfer.exceptions;

import java.lang.reflect.Type;

/**
 * 不支持的编码异常
 * Created by Jake on 2015/2/23.
 */
public class UnsupportSerializerTypeException extends RuntimeException {

    public UnsupportSerializerTypeException(Type type) {
        super("不支持的序列化类型:" + type);
    }

}
