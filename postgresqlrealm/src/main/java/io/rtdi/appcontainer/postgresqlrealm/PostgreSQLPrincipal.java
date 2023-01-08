package io.rtdi.appcontainer.postgresqlrealm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;
import io.rtdi.appcontainer.databaseloginrealm.RoleProcessor;

public class PostgreSQLPrincipal extends DatabaseLoginPrincipal {
	private static final long serialVersionUID = 4658263939802332L;
	public static final String JDBC_DRIVER = "org.postgresql.Driver";
	public static final String ROLE_QUERY = "select rolname \r\n"
			+ "from pg_roles \r\n"
			+ "where pg_has_role(current_user, rolname, 'member')";
	public static final String DBVERSION_QUERY = "select version()";
	public static final String DBUSER_QUERY = "select current_user";

	public PostgreSQLPrincipal(String name, String password, String jdbcurl) throws SQLException {
		super(name, password, jdbcurl, JDBC_DRIVER, ROLE_QUERY, new RoleProcessor(), DBVERSION_QUERY, DBUSER_QUERY);
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}

}
