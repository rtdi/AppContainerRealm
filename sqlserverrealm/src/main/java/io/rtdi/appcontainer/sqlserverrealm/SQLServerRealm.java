package io.rtdi.appcontainer.sqlserverrealm;

import java.sql.SQLException;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginRealm;

public class SQLServerRealm extends DatabaseLoginRealm {

	public SQLServerRealm() {
		super();
	}
	
	@Override
	protected SQLServerPrincipal createNewPrincipal(String username, String credentials, String jdbcurl) throws SQLException {
		return new SQLServerPrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
	}
	
}
