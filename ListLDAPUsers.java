package org.myproject.sample.core.servlets;

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



@Component(service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "=Ldap Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/ldaptest",
		"sling.servlet.extensions=" + "json" })
public class ListLDAPUsers extends SlingAllMethodsServlet {
	private static final Logger log = LoggerFactory.getLogger(ListLDAPUsers.class);


	@Override
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			addJSONHeaders(response);

			// JSONObject jsonObject = new JSONObject();

			Properties environment = new Properties();
			

			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, "ldap://localhost:10389");
			environment.put(Context.SECURITY_AUTHENTICATION, "simple");
			environment.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
			environment.put(Context.SECURITY_CREDENTIALS, "secret");

			DirContext context = new InitialDirContext(environment);
			log.debug("Connected..");
			
			String searchFilter = "(objectClass=inetOrgPerson)";
			String[] requiredAttributes = { "sn", "cn" };
			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			sc.setReturningAttributes(requiredAttributes);
			NamingEnumeration users = context.search("ou=users,ou=system", searchFilter, sc);

			SearchResult searchResult = null;
			

			while (users.hasMore()) {
				searchResult = (SearchResult) users.next();
				Attributes attr = searchResult.getAttributes();
				log.debug(attr.get("cn").get(0).toString());

			}
			System.out.println("end");
			context.close();
		} catch (AuthenticationNotSupportedException exception) {
			log.debug("The authentication is not supported by the server");
		}

		catch (AuthenticationException exception) {
			log.debug("Incorrect password or username");
		}

		catch (NamingException exception) {
			exception.printStackTrace();
			log.debug("Error when trying to create the context" + exception.getExplanation());
			log.error("Error", exception);

		}
	}

	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public static void addJSONHeaders(SlingHttpServletResponse response) {
		response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");
	}
}