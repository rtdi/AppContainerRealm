package io.rtdi.appcontainer.abaprealm;

import java.sql.SQLException;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginRealm;

public class AbapRealm extends DatabaseLoginRealm {

	public AbapRealm() {
		super();
	}
	
	@Override
	protected AbapPrincipal createNewPrincipal(String username, String credentials, String jdbcurl) throws SQLException {
		return new AbapPrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
	}
	
}
