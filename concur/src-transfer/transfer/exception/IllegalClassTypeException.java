package transfer.exception;

import java.lang.reflect.Type;

/**
 * 对象的类型错误
 * Created by Jake on 2015/2/24.
 */
public class IllegalClassTypeException extends RuntimeException {

    public IllegalClassTypeException(int classId, Type type) {
        super("对象类型错误,classId:" + classId + ",type:" + type);
    }

}
