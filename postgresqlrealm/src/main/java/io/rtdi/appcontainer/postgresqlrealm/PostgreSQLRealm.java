package io.rtdi.appcontainer.postgresqlrealm;

import java.sql.SQLException;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginRealm;

public class PostgreSQLRealm extends DatabaseLoginRealm {

	public PostgreSQLRealm() {
		super();
	}
	
	@Override
	protected PostgreSQLPrincipal createNewPrincipal(String username, String credentials, String jdbcurl) throws SQLException {
		return new PostgreSQLPrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
	}
	
}
