package utils.enhance.asm;

/**
 * asm增强异常
 * <br/>抛此异常将导致无法正常运行程序
 * Created by Jake on 2015/3/29.
 */
public class EnhanceAccessException extends RuntimeException {

    // 构造方法
    public EnhanceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
