package dbcache.support.jdbc;

/**
 * Jdbc执行异常
 * 
 * @author Jake
 *
 */
public class JdbcExecuteException extends RuntimeException {

	private static final long serialVersionUID = 502967541401975735L;

	public JdbcExecuteException(Throwable cause) {
		super(cause);
	}
	
}
