package io.rtdi.appcontainer.databaseloginrealm;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.catalina.realm.RealmBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * There are two ways to set the jdbcurl
 * <OL>
 * <LI>
 * In the server.xml as property &lt;Realm className="io.rtdi.appcontainer.snowflakerealm.SnowflakeRealm" 
 *    JDBCURL="jdbc:snowflake://&lt;account_name&gt;.snowflakecomputing.com/?&lt;connection_params&gt;"/&gt;
 * </LI><LI>
 * As environment variable called JDBCURL
 * </LI>
 * </OL>
 *
 */
public abstract class DatabaseLoginRealm extends RealmBase {
    /**
     * Juli logging of Tomcat
     */
    protected final Log log;
    /**
     * Database jdbcurl
     */
    private String jdbcurl;
    /**
     * Map with all connected users and their Tomcat principals
     */
    private Map<String, DatabaseLoginPrincipal> userdirectory = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Creates a new DatabaseLoginRealm for Tomcat
	 */
	public DatabaseLoginRealm() {
		log = LogFactory.getLog(this.getClass());
	}

	@Override
	public Principal authenticate(String username, String credentials) {
		if (username == null || credentials == null) {
			// If either one is missing, nothing can be done to auth the user
			return null;
		}
		/*
		 * For basic auth without database sessions, e.g. when using rest only, the authentication is called with every single request.
		 * Hence it must be quick.
		 */
		DatabaseLoginPrincipal existingprincipal = getDatabaseLoginPrincipalPrincipal(username);
		if (existingprincipal != null) {
			String serverCredentials = existingprincipal.getPassword();
			if (getCredentialHandler().matches(credentials, serverCredentials)) {
				return existingprincipal;
			}
		}
		if (jdbcurl == null) {
			jdbcurl = System.getenv("JDBCURL");
			if (jdbcurl == null) {
				log.error("No jdbc-url configured, neither as property in the server.xml nor as environment variable JDBCURL");
				return null;
			}
		}
		try {
			DatabaseLoginPrincipal principal = createNewPrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
			userdirectory.put(username, principal);
			return principal;
		} catch (SQLException e) {
            if (log != null && log.isTraceEnabled()) {
            	log.trace("failed to login with the provided credentials for \"" + username + "\" with jdbc connection string \"" + jdbcurl + "\" and exception " + e.getMessage());
            }
            log.error(e);
			return null;
		}
	}
	
	
    /**
     * Periodically logout the users from the cache
     */
    @Override
    public void backgroundProcess() {
        if (log.isTraceEnabled()) {
            log.trace("Cleaning outdated DatabasePricipals");
        }
		Iterator<String> iter = userdirectory.keySet().iterator();
		long current = System.currentTimeMillis();
		int count = 0;
		while (iter.hasNext()) {
			String key = iter.next();
			DatabaseLoginPrincipal p = userdirectory.get(key);
			if (current > p.getValidUntil()) {
				count++;
				userdirectory.remove(key);
				try {
					p.logout();
				} catch (Exception e) {
					// ignore
				}
			}
		}
        if (log.isTraceEnabled()) {
            log.trace("Logout of " + count + " DatabasePricipals during cleanup");
        }
	}

	/**
	 * Create a new realm for the provided database user
	 * @param username of the db
	 * @param credentials password
	 * @param jdbcurl of the connected database
	 * @return the Tomcat principal
	 * @throws SQLException in case of an error
	 */
	protected abstract DatabaseLoginPrincipal createNewPrincipal(String username, String credentials, String jdbcurl) throws SQLException;
	
	/**
	 * Actually returns null for security reasons
	 */
	@Override
	protected String getPassword(String username) {
		return null; // Do not expose the password. The RealmBase methods using this cannot be used anyhow.
	}

	/**
	 * Get the Principal associated with the specified user
	 * @return Principal
	 */
	@Override
	protected Principal getPrincipal(String username) {
		return userdirectory.get(username);
	}
	
	protected DatabaseLoginPrincipal getDatabaseLoginPrincipalPrincipal(String username) {
		return userdirectory.get(username);
	}
	
	/**
	 * @return JDBC URL of the used database
	 */
	public String getJDBCURL() {
		return jdbcurl;
	}

	/**
	 * @param jdbcurl the JDBC URL to be used
	 */
	public void setJDBCURL(String jdbcurl) {
		this.jdbcurl = jdbcurl;
	}

}
