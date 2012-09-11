/*
 *  Copyright 2012 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.repository.connector.remoting.davex;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * JNDI Resource Factory to create JCR Repository by instantiating {@link DavExHippoRepository}
 * 
 * JNDI Resource can be configured in the application context descriptor like the following for Tomcat:
 * 
 * <code><pre>
 * &lt;Context ...>
 *   ...
 *   &lt;Resource name="jcr/davexRepository" auth="Container"
 *             type="org.onehippo.forge.repository.connector.remoting.davex.HippoJcrRepository"
 *             factory="org.onehippo.forge.repository.connector.remoting.davex.DavExHippoRepositoryBasedHippoJcrRepositoryFactory"
 *             repositoryAddress="http://localhost:8080/cms/server" />
 *   ...
 * &lt;/Context>
 * </pre></code>
 * <BR/>
 * In addition, you should modify the web application deployment descriptor (/WEB-INF/web.xml) to
 * declare the JNDI name under which you will look up preconfigured repository in the above like the following:
 * <code><pre>
 * &lt;resource-ref>
 *   &lt;description>JCR Repository&lt;/description>
 *   &lt;res-ref-name>jcr/davexRepository&lt;/res-ref-name>
 *   &lt;res-type>org.onehippo.forge.repository.connector.remoting.davex.HippoJcrRepository&lt;/res-type>
 *   &lt;res-auth>Container&lt;/res-auth>
 * &lt;/resource-ref>
 * </pre></code>
 * <BR/>
 * Finally, you can write codes in a JSP page to test it like the following example:
 * <code><pre>
 * &lt;%@ page language="java" import="javax.jcr.*, javax.naming.*" %>
 * &lt;%
 * Context initCtx = new InitialContext();
 * Context envCtx = (Context) initCtx.lookup("java:comp/env");
 * Repository repository = (Repository) envCtx.lookup("jcr/davexRepository");
 * Session jcrSession = repository.login();
 * // do something...
 * jcrSession.logout();
 * %>
 * </pre></code>
 * <P>Also, you can get HippoRepository instance through HippoRepositoryFactory if you want:</P>
 * <code><pre>
 * &lt;%@ page language="java" import="javax.jcr.*, javax.naming.*, org.hippoecm.repository.*" %>
 * &lt;%
 * HippoRepository hippoRepository = HippoRepositoryFactory("java:comp/env/jcr/davexRepository");
 * Session jcrSession = repository.login(new SimpleCredentials("siteuser", "siteuser".toCharArray());
 * // do something...
 * jcrSession.logout();
 * %>
 * </pre></code>
 */
public class DavExHippoRepositoryBasedHippoJcrRepositoryFactory implements ObjectFactory {
    
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        String repositoryAddress = null;

        if (obj instanceof Reference) {
            Enumeration<RefAddr> addrs = ((Reference) obj).getAll();

            while (addrs.hasMoreElements()) {
                RefAddr addr = (RefAddr) addrs.nextElement();
                String type = addr.getType();
                String value = (String) addr.getContent();

                if ("repositoryAddress".equals(type)) {
                    if (value != null && !"".equals(value.trim())) {
                        repositoryAddress = value;
                        break;
                    }
                }
            }
        }

        if (repositoryAddress == null) {
            throw new RuntimeException("Invalid repository address: " + repositoryAddress);
        }

        return new LazyHippoJcrRepository(repositoryAddress);
    }
}