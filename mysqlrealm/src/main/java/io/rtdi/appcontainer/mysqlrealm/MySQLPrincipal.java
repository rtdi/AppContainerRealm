package io.rtdi.appcontainer.mysqlrealm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the database version.
 *
 */
public class MySQLPrincipal extends DatabaseLoginPrincipal {
	private static final long serialVersionUID = 46582673892656223L;
	public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	/**
	 * Might be replaces with 
	 * select ROLE_NAME from information_schema.APPLICABLE_ROLES
	 * as this is available since 8.0.19
	 */
	public static final String ROLE_QUERY = "show grants";
	public static final String DBVERSION_QUERY = "select VERSION()";
	/*
	 * The dbusername is the user part without the ...@host suffix
	 * 
	 * Might be later be replace with
	 * select user from information_schema.USER_ATTRIBUTES
	 * as this view is available in 8.0.21
	 */
	public static final String DBUSER_QUERY = "select case when locate('@', current_user()) = 0 "
			+ "then current_user() "
			+ "else "
			+ "substring(current_user(), 1, locate('@', current_user())-1) "
			+ "end";

	public MySQLPrincipal(String name, String password, String jdbcurl) throws SQLException {
		super(name, password, modifyJDBCUrl(jdbcurl), JDBC_DRIVER, ROLE_QUERY, new MySQLRoleProcessor(), DBVERSION_QUERY, DBUSER_QUERY);
	}

	/**
	 * Unless the user specifies it, the default shall be that a MySQL database is a schema, not a catalog.
	 * 
	 * @param jdbcurl is the original jdbc url
	 * @return the input jdbcurl with the query parameter databaseTerm=SCHEMA added unless the url contains that already 
	 */
	private static String modifyJDBCUrl(String jdbcurl) {
		if (jdbcurl.contains("databaseTerm=")) {
			return jdbcurl;
		} else {
			if (jdbcurl.contains("?")) {
				return jdbcurl + "&databaseTerm=SCHEMA";
			} else {
				return jdbcurl + "?databaseTerm=SCHEMA";
			}
		}
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}

}
