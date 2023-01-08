package io.rtdi.appcontainer.databaseloginrealm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Validates the provided login data against the database and queries the database roles and other meta information
 *
 */
public abstract class DatabaseLoginPrincipal extends GenericPrincipal implements IDatabaseLoginPrincipal {

	private static final long serialVersionUID = 486703493493L;
	/**
	 * Version of the connected database
	 */
	private String version;
	/**
	 * database username
	 */
	private String user;
	/**
	 * Optional information about login warnings like must-change-password
	 */
	private Optional<String> loginwarnings;
	/**
	 * Tomcat jdbc connection pool
	 */
	private DataSource pool;
	/**
	 * The real database schema connected to
	 */
	private String schema;
	/**
	 * The jdbc driver version used
	 */
	private String driverversion;
	/**
	 * The jdbc driver name used
	 */
	private String jdbcdriver;

	/**
	 * @param name database user name
	 * @param password database password
	 * @param jdbcurl database JDBC connection URL
	 * @param jdbcdriver class name of the JDBC driver to use
	 * @param roleSql a select with a single string column containing all database roles assigned to that user
	 * @param versionSql a select statement executed to find the database version
	 * @param currentuserSql a select statement executed to find the exact user name of the connected user
	 * @throws SQLException in case the login sequence cannot be performed completely
	 */
	public DatabaseLoginPrincipal(String name, String password, String jdbcurl, String jdbcdriver, String roleSql, RoleProcessor processor, String versionSql, String currentuserSql) throws SQLException {
		super(name, queryRoles(name, password, jdbcurl, jdbcdriver, roleSql, processor)); // unfortunately there is no better way than that because of the Tomcat Principal constructor
		pool = getDataSource(name, password, jdbcurl, jdbcdriver, versionSql, currentuserSql);
		this.jdbcdriver = jdbcdriver;
	}

	/**
	 * @param name database user name
	 * @param password database password
	 * @param jdbcurl database JDBC connection URL
	 * @param jdbcdriver class name of the JDBC driver to use
	 * @param processor 
	 * @param roleSQL a select with a single string column containing all database roles assigned to that user
	 * @return the list of database role names the user has assigned, direct or indirect
	 * @throws SQLException in case the roles cannot be read
	 */
	private static List<String> queryRoles(String name, String password, String jdbcurl, String jdbcdriver, String roleSql, RoleProcessor processor) throws SQLException {
		try (Connection c = getDatabaseConnection(name, password, jdbcurl, jdbcdriver)) {
			try (PreparedStatement stmt = c.prepareStatement(roleSql); ) {
				List<String> roles = new ArrayList<String>();
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String rolename = processor.processRole(rs.getString(1));
					if (rolename != null) {
						roles.add(rolename);
					}
				}
				roles.add("PUBLIC"); // an authenticated user is always part of the PUBLIC group
				return roles;
			} catch (SQLException e) {
				throw new LoginSQLException(e, roleSql);
			}
		}
	}
	
	private static Connection getDatabaseConnection(String user, String passwd, String jdbcurl, String jdbcdriver) throws SQLException {
        // Class.forName(jdbcdriver);   No longer needed with all JDBC drivers
		return DriverManager.getConnection(jdbcurl, user, passwd);
	}

	@Override
	public String getDriver() {
		return jdbcdriver;
	}

	@Override
	public String getDBVersion() {
		return version;
	}

	@Override
	public String getDBUser() {
		return user;
	}

	@Override
	public Optional<String> getLoginWarnings() {
		return loginwarnings;
	}

	private DataSource getDataSource(String name, String password, String jdbcurl, String jdbcdriver, String versionquery, String currentuserquery) throws SQLException {
		DataSource datasource = null;
        PoolProperties p = new PoolProperties();
        p.setUrl(jdbcurl);
        p.setDriverClassName(jdbcdriver);
        p.setUsername(name);
        p.setPassword(password);
        p.setJmxEnabled(false);
        p.setTestWhileIdle(false);
        // p.setTestOnBorrow(true);
        // p.setValidationQuery("SELECT 1");
        // p.setTestOnReturn(false);
        // p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(3);
        p.setInitialSize(1);
        p.setMaxIdle(p.getMaxActive());
        p.setMinIdle(p.getMaxActive());
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        // p.setMinEvictableIdleTimeMillis(60000);
        // p.setMinIdle(10);
        // p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setDefaultAutoCommit(false);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        setAdditionalJDBCPoolProperties(p);
        datasource = new DataSource();
        datasource.setPoolProperties(p);
		try (Connection c = datasource.getConnection()) {
			this.schema = c.getSchema();
			driverversion = c.getMetaData().getDriverVersion();
			this.version = readDatabaseVersion(c, versionquery);
			this.user = readExactUserName(c, currentuserquery);
			this.loginwarnings = validateLogin(c);
		}		
        return datasource;
	}

	/**
	 * A hook to set additional values in p, which is then passed to datasource.setPoolProperties(p);
	 * Only a last reserve, should be the empty implementation most of the time.
	 * 
	 * @param p Tomcat prepopulated PoolProperties object the overwrite some
	 */
	protected void setAdditionalJDBCPoolProperties(PoolProperties p) {
	}

	@Override
	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	@Override
	public String getDriverVersion() {
		return driverversion;
	}

	@Override
	public String getSchema() {
		return schema;
	}

	/**
	 * This method should validate the connected user and return a string with information useful to the end user.<br>
	 * Examples of tests and information to be returned may include:
	 * <ul>
	 * <li>Password will expire in one week</li>
	 * <li>Password must be changed immediately</li>
	 * </ul> 
	 *  
	 * @param c a JDBC connection to retrieve the data
	 * @return a text with maybe a HTML link or null
	 * @throws LoginSQLException in case of SQL errors
	 */
	public abstract Optional<String> validateLogin(Connection c) throws LoginSQLException;
	
	/**
	 * @param c a JDBC connection to retrieve the data
	 * @param versionquery 
	 * @return a string indicating the connected database version
	 * @throws LoginSQLException in case of SQL errors
	 */
	public String readDatabaseVersion(Connection c, String versionquery) throws LoginSQLException {
		try (PreparedStatement stmt = c.prepareStatement(versionquery); ) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return "database version unknown";
			}
		} catch (SQLException e) {
			throw new LoginSQLException("Failed to read the database version", e, versionquery);
		}
	}
	
	/**
	 * @param c a JDBC connection to retrieve the data
	 * @param currentuserquery 
	 * @return the exact username as known by the database, e.g. the login was 'user1' but the actual user name is 'USER1'
	 * @throws LoginSQLException in case of SQL errors
	 */
	public String readExactUserName(Connection c, String currentuserquery) throws LoginSQLException {
		try (PreparedStatement stmt = c.prepareStatement(currentuserquery); ) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return this.getName();
			}
		} catch (SQLException e) {
			throw new LoginSQLException("Failed to read the database user", e, currentuserquery);
		}
	}

	@Override
	public void logout() throws Exception {
		if (pool != null) {
			pool.close();
		}
		super.logout();
	}
}
