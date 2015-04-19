package transfer.exceptions;

/**
 * 不支持的类类型
 * Created by Jake on 2015/2/25.
 */
public class UnsupportClassException extends RuntimeException {

    public UnsupportClassException(int id) {
        super("不支持的类类型,id:" + id);
    }

    public UnsupportClassException(Class<?> clazz) {
        super("不支持的类类型:" + clazz);
    }
}
