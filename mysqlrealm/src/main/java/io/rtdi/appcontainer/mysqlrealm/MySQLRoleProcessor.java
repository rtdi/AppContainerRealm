package io.rtdi.appcontainer.mysqlrealm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rtdi.appcontainer.databaseloginrealm.RoleProcessor;

public class MySQLRoleProcessor extends RoleProcessor {
	private static final String ROLEPATTERN = "GRANT `(\\w*)`.*";
	private static final Pattern pattern = Pattern.compile(ROLEPATTERN);

	/**
	 * show grants returns two types of grants:
	 * <OL><LI>
	 * GRANT `main_read_only`@`%` TO `dbrealmtest`@``
	 * </LI><LI>
	 * GRANT USAGE ON *.* TO `dbrealmtest`@``
	 * </LI></OL>
	 * 
	 * Only the second one is of interest and must be parsed
	 */
	@Override
	public String processRole(String dbrolename) {
		Matcher matcher = pattern.matcher(dbrolename);
		if (matcher.matches() && matcher.groupCount() > 0) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
	
}
