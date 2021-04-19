package io.rtdi.appcontainer.realm;


import java.sql.Connection;
import java.sql.SQLException;

public interface IAppContainerPrincipal {

	/**
	 * @return the database connection JDBC URL used
	 */
	String getJDBCURL();

	Connection createNewConnection() throws SQLException;

	String getDriverURL();

	/**
	 * @return the version string of the connected database as retrieved at login
	 */
	String getDBVersion();

	/**
	 * @return the exact user, e.g. the loginuser might by user1 but the actual database user name is "USER1"
	 */
	String getDBUser();

	String getPassword();

	String[] getRoles();

}