package io.rtdi.appcontainer.mysqlrealm;

import java.sql.SQLException;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginRealm;

public class MySQLRealm extends DatabaseLoginRealm {

	public MySQLRealm() {
		super();
	}
	
	@Override
	protected MySQLPrincipal createNewPrincipal(String username, String credentials, String jdbcurl) throws SQLException {
		return new MySQLPrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
	}
	
}
