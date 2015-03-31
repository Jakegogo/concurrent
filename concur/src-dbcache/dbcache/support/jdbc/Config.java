package dbcache.support.jdbc;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class Config {

	String name;

	private final ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();

	@Autowired
	DataSource dataSource;

	int transactionLevel = Connection.TRANSACTION_READ_COMMITTED;

	boolean showSql = true;
	boolean devMode = false;
	Dialect dialect = Dialect.getDefaultDialect();

	/**
	 * For DbKit.brokenConfig = new Config();
	 */
	Config() {

	}

	/**
	 * Constructor with DataSource
	 * @param dataSource the dataSource, can not be null
	 */
	public Config(String name, DataSource dataSource) {
		if (StringUtils.isBlank(name))
			throw new IllegalArgumentException("Config name can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("DataSource can not be null");

		this.name = name.trim();
		this.dataSource = dataSource;
	}

	/**
	 * Constructor with DataSource and Dialect
	 * @param dataSource the dataSource, can not be null
	 * @param dialect the dialect, can not be null
	 */
	public Config(String name, DataSource dataSource, Dialect dialect) {
		if (StringUtils.isBlank(name))
			throw new IllegalArgumentException("Config name can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("DataSource can not be null");
		if (dialect == null)
			throw new IllegalArgumentException("Dialect can not be null");

		this.name = name.trim();
		this.dataSource = dataSource;
		this.dialect = dialect;
	}

	/**
	 * Constructor with full parameters
	 * @param dataSource the dataSource, can not be null
	 * @param dialect the dialect, set null with default value: new MysqlDialect()
	 * @param showSql the showSql,set null with default value: false
	 * @param devMode the devMode, set null with default value: false
	 * @param transactionLevel the transaction level, set null with default value: Connection.TRANSACTION_READ_COMMITTED
	 * @param containerFactory the containerFactory, set null with default value: new IContainerFactory(){......}
	 * @param cache the cache, set null with default value: new EhCache()
	 */
	public Config(String name,
				  DataSource dataSource,
				  Dialect dialect,
				  Boolean showSql,
				  Boolean devMode,
				  Integer transactionLevel) {
		if (StringUtils.isBlank(name))
			throw new IllegalArgumentException("Config name can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("DataSource can not be null");

		this.name = name.trim();
		this.dataSource = dataSource;

		if (dialect != null)
			this.dialect = dialect;
		if (showSql != null)
			this.showSql = showSql;
		if (devMode != null)
			this.devMode = devMode;
		if (transactionLevel != null)
			this.transactionLevel = transactionLevel;
	}

	public String getName() {
		return name;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public int getTransactionLevel() {
		return transactionLevel;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public boolean isDevMode() {
		return devMode;
	}

	// --------

	/**
	 * Support transaction with Transaction interceptor
	 */
	public final void setThreadLocalConnection(Connection connection) {
		threadLocal.set(connection);
	}

	public final void removeThreadLocalConnection() {
		threadLocal.remove();
	}

	/**
	 * Get Connection. Support transaction if Connection in ThreadLocal
	 */
	public final Connection getConnection() throws SQLException {
		Connection conn = threadLocal.get();
		if (conn != null) {
			threadLocal.set(conn);
			return conn;
		}
		return showSql ? new SqlReporter(dataSource.getConnection()).getConnection() : dataSource.getConnection();
	}

	/**
	 * Helps to implement nested transaction.
	 * Tx.intercept(...) and Db.tx(...) need this method to detected if it in nested transaction.
	 */
	public final Connection getThreadLocalConnection() {
		return threadLocal.get();
	}

	/**
	 * Close ResultSet、Statement、Connection
	 * ThreadLocal support declare transaction.
	 */
	public final void close(ResultSet rs, Statement st, Connection conn) {
		if (rs != null) {try {rs.close();} catch (SQLException e) {e.printStackTrace();}}
		if (st != null) {try {st.close();} catch (SQLException e) {e.printStackTrace();}}

		if (threadLocal.get() == null) {	// in transaction if conn in threadlocal
			if (conn != null) {try {conn.close();}
			catch (SQLException e) {throw new IllegalStateException(e);}}
		}
	}

	public final void close(Statement st, Connection conn) {
		if (st != null) {try {st.close();} catch (SQLException e) {}}

		if (threadLocal.get() == null) {	// in transaction if conn in threadlocal
			if (conn != null) {try {conn.close();}
			catch (SQLException e) {throw new IllegalStateException(e);}}
		}
	}

	public final boolean checkConnection(Connection conn) {
		boolean isConnection = false;
		
		try {
			if (conn.isValid(2)) {
				isConnection = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (!isConnection) {
			threadLocal.set(null);
		}
		return isConnection;
	}

	public final void close(Connection conn) {
		if (threadLocal.get() == null)		// in transaction if conn in threadlocal
			if (conn != null)
				try {conn.close();} catch (SQLException e) {throw new IllegalStateException(e);}
	}
}