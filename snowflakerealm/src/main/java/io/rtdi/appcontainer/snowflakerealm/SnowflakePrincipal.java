package io.rtdi.appcontainer.snowflakerealm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;
import io.rtdi.appcontainer.databaseloginrealm.RoleProcessor;

public class SnowflakePrincipal extends DatabaseLoginPrincipal {

	private static final long serialVersionUID = 742580325432L;
	public static final String JDBC_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";
	public static final String ROLE_QUERY = "select role_name from UTIL_DB.INFORMATION_SCHEMA.APPLICABLE_ROLES";
	public static final String DBVERSION_QUERY = "select current_version()";
	public static final String DBUSER_QUERY = "select current_user()";

	public SnowflakePrincipal(String name, String password, String jdbcurl) throws SQLException {
		super(name, password, jdbcurl, JDBC_DRIVER, ROLE_QUERY, new RoleProcessor(), DBVERSION_QUERY, DBUSER_QUERY);
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}

}
