package dbcache.support.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SqlReporter.
 */
public class SqlReporter implements InvocationHandler {
	
	private Connection conn;
	private static boolean loggerOn = false;
	private static final Logger log = LoggerFactory.getLogger(SqlReporter.class);
	
	SqlReporter(Connection conn) {
		this.conn = conn;
	}
	
	public static void setLogger(boolean on) {
		SqlReporter.loggerOn = on;
	}
	
	@SuppressWarnings("rawtypes")
	Connection getConnection() {
		Class clazz = conn.getClass();
		return (Connection)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{Connection.class}, this);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (method.getName().equals("prepareStatement")) {
				String info = "Sql: " + args[0];
				if (loggerOn)
					log.info(info);
				else
					System.out.println(info);
			}
			return method.invoke(conn, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}
