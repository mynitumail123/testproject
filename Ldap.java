package org.testsite.customlogin.authentication.impl;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class Ldap
{
    public static void main(String[]args)
    {
        Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        environment.put(Context.SECURITY_CREDENTIALS, "secret");

        try 
        {
            DirContext context = new InitialDirContext(environment);
            System.out.println("Connected..");
            System.out.println(context.getEnvironment());
            
            String searchFilter = "(objectClass=inetOrgPerson)";
            String[] requiredAttributes = {"sn","cn"};
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setReturningAttributes(requiredAttributes);
            
            NamingEnumeration users = context.search("ou=users,ou=system", searchFilter, sc);
            
            SearchResult searchResult = null;
            System.out.println("---------------");
            
            while(users.hasMore()){
            	searchResult = (SearchResult) users.next();
            	Attributes attr = searchResult.getAttributes();
            	System.out.println(attr.get("cn").get(0).toString());
            	
            }
            System.out.println("end");
            context.close();
        } 
        catch (AuthenticationNotSupportedException exception) 
        {
            System.out.println("The authentication is not supported by the server");
        }

        catch (AuthenticationException exception)
        {
            System.out.println("Incorrect password or username");
        }

        catch (NamingException exception)
        {
            System.out.println("Error when trying to create the context");
            exception.printStackTrace();
            
        }
    }
}
