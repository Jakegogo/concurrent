package dbcache;

/**
 * Db cache Service初始化异常
 * <br/>抛此异常将导致无法正常运行程序
 * Created by Jake on 2015/3/29.
 */
public class DbCacheInitError extends RuntimeException {
	private static final long serialVersionUID = 1059261362624198157L;

	// 构造方法
    public DbCacheInitError(String message) {
        super(message);
    }
	
    // 构造方法
    public DbCacheInitError(String message, Throwable cause) {
        super(message, cause);
    }
}
