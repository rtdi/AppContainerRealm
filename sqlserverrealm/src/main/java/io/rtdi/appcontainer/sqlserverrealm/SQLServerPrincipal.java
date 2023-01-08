package io.rtdi.appcontainer.sqlserverrealm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;
import io.rtdi.appcontainer.databaseloginrealm.RoleProcessor;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the database version.
 *
 */
public class SQLServerPrincipal extends DatabaseLoginPrincipal {
	private static final long serialVersionUID = 4658263939892656292L;
	public static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String ROLE_QUERY = "SELECT r.name\r\n"
			+ "  FROM sys.database_role_members AS m\r\n"
			+ "  INNER JOIN sys.database_principals AS r\r\n"
			+ "  ON m.role_principal_id = r.principal_id\r\n"
			+ "  INNER JOIN sys.database_principals AS u\r\n"
			+ "  ON u.principal_id = m.member_principal_id\r\n"
			+ "  WHERE u.name = CURRENT_USER";
	public static final String DBVERSION_QUERY = "select @@VERSION";
	public static final String DBUSER_QUERY = "select CURRENT_USER";

	public SQLServerPrincipal(String name, String password, String jdbcurl) throws SQLException {
		super(name, password, jdbcurl, JDBC_DRIVER, ROLE_QUERY, new RoleProcessor(), DBVERSION_QUERY, DBUSER_QUERY);
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}

}
