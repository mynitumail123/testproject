package org.myproject.sample.core.servlets;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Properties;



@Component(service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "=Ldap Servlet Connection",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/ldaptestcon",
		"sling.servlet.extensions=" + "json" })
public class ListLDAPUsersConnection extends SlingAllMethodsServlet {
	private static final Logger log = LoggerFactory.getLogger(ListLDAPUsersConnection.class);


	@Override
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		// test server
				String host = "localhost";
				int port = 10389;
				String user = "uid=admin,ou=system";
				String pass = "secret";

				
				try (LdapConnection conn = createLdapConnection(host, port, user, pass); // default apache ds password
				) {
					//Dn base = new Dn("dc=demo1,dc=com");
					Dn base = new Dn("cn=Cognizant,ou=users,ou=system");
					searchLdap(conn, base, "(objectClass=*)");	// simple searching
				} catch (IOException | LdapException e) {
					log.error("Error in ====> ", e);
				}
	}

	private LdapConnection createLdapConnection(String host, int port, String user, String pass) {
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost(host);
		config.setLdapPort(port);
		config.setName(user);
		config.setCredentials(pass);

		LdapNetworkConnection conn = new LdapNetworkConnection(config);
		return conn;
	}

	private void searchLdap(LdapConnection conn, Dn baseDn, String filter) throws LdapException {
		conn.bind();
		EntryCursor search = conn.search(baseDn, filter, SearchScope.SUBTREE);
		for (Entry e : search) {
			log.debug("LdapSearch01.searchLdap()");
			log.debug(" Searched items in LDAP is",e.getDn().getNormName());
		}

		conn.unBind();
	}
}