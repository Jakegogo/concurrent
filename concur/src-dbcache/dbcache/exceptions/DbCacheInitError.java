package dbcache.exceptions;

/**
 * Db cache Service初始化异常
 * Created by Jake on 2015/3/29.
 */
public class DbCacheInitError extends RuntimeException {

    // 构造方法
    public DbCacheInitError(String message, Throwable cause) {
        super(message, cause);
    }
}
