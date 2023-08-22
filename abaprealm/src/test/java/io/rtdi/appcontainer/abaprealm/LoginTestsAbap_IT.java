package io.rtdi.appcontainer.abaprealm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.rtdi.appcontainer.databaseloginrealm.DatabaseLoginPrincipal;
import io.rtdi.appcontainer.databaseloginrealm.TestBase;

class LoginTestsAbap_IT extends TestBase {

	protected LoginTestsAbap_IT() throws LoginException {
		super("abap", new AbapRealm());
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
			assertTrue(getJDBCUser().equalsIgnoreCase(principal.getDBUser()));
			assertNotNull(principal.getDBVersion(), "Version can be any valid string");
			assertNotNull(principal.getDriver(), "Driver can be any valid string");
			assertNotNull(principal.getDriverVersion(), "Version can be any valid string");
			assertTrue(Arrays.binarySearch(principal.getRoles(), "PUBLIC") >= 0);
			
			/*
			 * Validate the user can not only be connected but also the Tomcat Connection Pool works
			 */
			try (Connection conn = principal.getConnection();) {
				executeQuery(conn, "select * from DD03L where tabname = 'DD02L' and as4local = 'A'");
			} finally {
				principal.logout();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

}
