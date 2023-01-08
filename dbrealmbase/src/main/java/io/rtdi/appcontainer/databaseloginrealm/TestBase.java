package io.rtdi.appcontainer.databaseloginrealm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;

/**
 * Used for Unit tests only.
 * Not happy with having a test class in the actual code but okay.
 *
 */
public abstract class TestBase {
	
	private String jdbcuser;
	private String jdbccredential;
	private String jdbcurl;
	protected DatabaseLoginRealm realm;

	protected TestBase(String dbtype, DatabaseLoginRealm realm) throws LoginException {
		this.jdbcuser = System.getenv(dbtype + "_jdbcuser");
		this.jdbccredential = System.getenv(dbtype + "_jdbccredential");
		this.jdbcurl = System.getenv(dbtype + "_jdbcurl");
		if (this.jdbcurl == null) {
			throw new LoginException(String.format("No environment variables found for test database connection: %s_jdbcurl, %s_jdbcuser, %s_jdbccredential",
					dbtype, dbtype, dbtype));
		}
		this.realm = realm;
		realm.setJDBCURL(jdbcurl);
	}
	
	protected String getJDBCUser() {
		return jdbcuser;
	}

	protected String getJDBCCredential() {
		return jdbccredential;
	}

	protected String getJDBCUrl() {
		return jdbcurl;
	}

	protected static int executeQuery(Connection conn, String sql) throws SQLException {
		try (PreparedStatement stmt = conn.prepareStatement(sql);) {
			try (ResultSet rs = stmt.executeQuery();) {
				int count = 0;
				while (rs.next()) {
					count++;
				}
				return count;
			}
		}
	}
}
