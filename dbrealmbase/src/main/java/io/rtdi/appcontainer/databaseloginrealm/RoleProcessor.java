package io.rtdi.appcontainer.databaseloginrealm;

/**
 * Default RoleProcessor returns all role names as is.<br>
 * For some databases the roles cannot be read and manipulated with SQL selects, e.g. MySQL only supports the show grants command to get all roles without any special permissions.
 * This class allows to post process the result.
 *
 */
public class RoleProcessor {

	/**
	 * The default operation is to simply return the input string. But if that string should be modified this can be done by overriding this method.
	 * To ignore a value the method returns null. 
	 * 
	 * @param dbrolename as sent by the database
	 * @return the converted realm role name or null in case the role should not be used in the realm
	 */
	public String processRole(String dbrolename) {
		return dbrolename;
	}
}
