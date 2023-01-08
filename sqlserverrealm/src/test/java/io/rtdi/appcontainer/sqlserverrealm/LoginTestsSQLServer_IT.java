package io.rtdi.appcontainer.sqlserverrealm;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.TestBase;

class LoginTestsSQLServer_IT extends TestBase {

	protected LoginTestsSQLServer_IT() throws LoginException {
		super("sqlserver", new SQLServerRealm());
	}

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void test() {
		try {
			DatabaseLoginPrincipal principal = (DatabaseLoginPrincipal) realm.authenticate(getJDBCUser(), getJDBCCredential());
			assertNotNull(principal, "principal was not loaded due to an error");
			assertEquals(getJDBCUser(), principal.getName(), "must return the exact same string");
			assertEquals("dbo", principal.getDBUser());
			assertNotNull(principal.getDBVersion(), "Version can be any valid string");
			assertNotNull(principal.getDriver(), "Driver can be any valid string");
			assertNotNull(principal.getDriverVersion(), "Version can be any valid string");
			assertTrue(Arrays.binarySearch(principal.getRoles(), "PUBLIC") >= 0);
			
			/*
			 * Validate the user can not only be connected but also the Tomcat Connection Pool works
			 */
			try (Connection conn = principal.getConnection();) {
				executeQuery(conn, "select * from information_schema.tables");
			} finally {
				principal.logout();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

}
