package io.rtdi.appcontainer.hanarealm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;
import io.rtdi.appcontainer.databaseloginrealm.RoleProcessor;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the Hana database version.
 *
 */
public class HanaPrincipal extends DatabaseLoginPrincipal {
	private static final long serialVersionUID = 4658263939892656292L;
	public static final String JDBC_DRIVER = "com.sap.db.jdbc.Driver";
	public static final String ROLE_QUERY = "select role_name from effective_roles where user_name = current_user";
	public static final String DBVERSION_QUERY = "select version from m_database";
	public static final String DBUSER_QUERY = "select current_user from dummy";

	public HanaPrincipal(String name, String password, String jdbcurl) throws SQLException {
		super(name, password, jdbcurl, JDBC_DRIVER, ROLE_QUERY, new RoleProcessor(), DBVERSION_QUERY, DBUSER_QUERY);
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}

}
