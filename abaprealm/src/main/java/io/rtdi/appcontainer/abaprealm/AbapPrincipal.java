package io.rtdi.appcontainer.abaprealm;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;
import io.rtdi.appcontainer.databaseloginrealm.RoleProcessor;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the Netweaver version.
 *
 */
public class AbapPrincipal extends DatabaseLoginPrincipal {
	private static final long serialVersionUID = 46582636292L;
	public static final String JDBC_DRIVER = "io.rtdi.jdbcabap.AbapDriver";
	public static final String DBVERSION_QUERY = "select RELEASE from CVERS where COMPONENT = 'SAP_ABA'";

	public AbapPrincipal(String name, String password, String jdbcurl) throws SQLException {
		super(name, password, jdbcurl, JDBC_DRIVER, getRoleSQL(name), new RoleProcessor(), DBVERSION_QUERY, null);
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}

	@Override
	public String readExactUserName(Connection c, String currentuserquery) throws LoginSQLException {
		return this.getName().toUpperCase();
	}
	
	private static String getRoleSQL(String name) {
		String today = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
		return String.format("select AGR_NAME from AGR_USERS where UNAME = '%s' and FROM_DAT <= '%s' and TO_DAT >= '%s'", name.toUpperCase(), today, today);
	}

}
