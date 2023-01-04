package io.rtdi.appcontainer.hanarealm;

import java.security.Principal;
import java.sql.SQLException;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginRealm;

public class HanaRealm extends DatabaseLoginRealm {

	public HanaRealm() {
		super();
	}
	
	@Override
	protected Principal createNewPrincipal(String username, String credentials, String jdbcurl) throws SQLException {
		return new HanaPrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
	}
	
}
