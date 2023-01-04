package io.rtdi.appcontainer.hanarealm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.LoginSQLException;

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
		super(name, password, jdbcurl, JDBC_DRIVER, ROLE_QUERY);
	}

	@Override
	public Optional<String> validateLogin(Connection c) throws LoginSQLException {
		return Optional.empty();
	}


	@Override
	public String readDatabaseVersion(Connection c) throws LoginSQLException {
		try (PreparedStatement stmt = c.prepareStatement(DBVERSION_QUERY); ) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return "database version unknown";
			}
		} catch (SQLException e) {
			throw new LoginSQLException("Failed to read the database version", e, DBVERSION_QUERY);
		}
	}


	@Override
	public String readExactUserName(Connection c) throws LoginSQLException {
		try (PreparedStatement stmt = c.prepareStatement(DBUSER_QUERY); ) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return this.getName();
			}
		} catch (SQLException e) {
			throw new LoginSQLException("Failed to read the database user", e, DBUSER_QUERY);
		}
	}

}
