package transfer.exceptions;

import java.lang.reflect.Type;

/**
 * 不支持的解析类型异常
 * Created by Jake on 2015/2/23.
 */
public class UnsupportDeserializerTypeException extends RuntimeException {

    public UnsupportDeserializerTypeException(byte byteType) {
        super("不支持解析类型:" + String.valueOf(byteType));
    }

    public UnsupportDeserializerTypeException(Type type) {
        super("不支持解析类型:" + type);
    }

    public UnsupportDeserializerTypeException(Type type, Exception e) {
        super("不支持解析类型:" + type, e);
    }

}
