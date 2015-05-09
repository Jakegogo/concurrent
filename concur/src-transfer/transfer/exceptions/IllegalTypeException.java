package transfer.exceptions;

import transfer.core.DeserialContext;

import java.lang.reflect.Type;

/**
 * 错误的类型异常
 * Created by Jake on 2015/2/23.
 */
public class IllegalTypeException extends RuntimeException {

    public IllegalTypeException(DeserialContext context, byte type, byte requiredType, Type type1) {
        super("类型错误:" + type + ",需要的类型:" + requiredType + "(" + type1 + ")" + context.getStackTrace());
    }

}
