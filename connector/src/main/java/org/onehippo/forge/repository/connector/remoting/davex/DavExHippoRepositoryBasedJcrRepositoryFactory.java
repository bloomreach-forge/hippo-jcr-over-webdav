/*
 *  Copyright 2008 Hippo.
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

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.hippoecm.repository.HippoRepository;

/**
 * JNDI Resource Factory to create JCR Repository by instantiating {@link DavExHippoRepository}
 * 
 * JNDI Resource can be configured in the application context descriptor like the following for Tomcat:
 * 
 * <code><pre>
 * &lt;Context ...>
 *   ...
 *   &lt;Resource name="jcr/davexRepository" auth="Container"
 *             type="javax.jcr.Repository"
 *             factory="org.onehippo.forge.repository.connector.remoting.davex.DavExHippoRepositoryBasedJcrRepositoryFactory"
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
 *   &lt;res-type>javax.jcr.Repository&lt;/res-type>
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
public class DavExHippoRepositoryBasedJcrRepositoryFactory implements ObjectFactory {
    
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

        return new LazyJcrRepositoryWrappingHippoRepository(repositoryAddress);
    }

    /**
     * JCR Repository implementation wrapping HippoRepository.
     */
    private static class LazyJcrRepositoryWrappingHippoRepository implements Repository {

        private String location;
        private HippoRepository hippoRepository;

        private LazyJcrRepositoryWrappingHippoRepository(String location) {
            if (location == null || "".equals(location.trim())) {
                throw new IllegalArgumentException("Invalid hippo repository location: " + location);
            }

            this.location = location;
        }

        public String getDescriptor(String key) {
            return getHippoRepository().getRepository().getDescriptor(key);
        }

        public String[] getDescriptorKeys() {
            return getHippoRepository().getRepository().getDescriptorKeys();
        }

        public Session login() throws LoginException, RepositoryException {
            return getHippoRepository().login();
        }

        public Session login(Credentials credentials) throws LoginException, RepositoryException {
            if (!(credentials instanceof SimpleCredentials)) {
                throw new IllegalArgumentException("Only javax.jcr.SimpleCredentials is supported.");
            }

            return getHippoRepository().login((SimpleCredentials) credentials);
        }

        public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
            return login();
        }

        public Session login(Credentials credentials, String workspaceName) throws LoginException,
                NoSuchWorkspaceException, RepositoryException {
            return login(credentials);
        }

        public Value getDescriptorValue(String key) {
            return getHippoRepository().getRepository().getDescriptorValue(key);
        }

        public Value[] getDescriptorValues(String key) {
            return getHippoRepository().getRepository().getDescriptorValues(key);
        }

        public boolean isSingleValueDescriptor(String key) {
            return getHippoRepository().getRepository().isSingleValueDescriptor(key);
        }

        public boolean isStandardDescriptor(String key) {
            return getHippoRepository().getRepository().isStandardDescriptor(key);
        }

        HippoRepository getHippoRepository() {
            if (hippoRepository == null) {
                try {
                    hippoRepository = DavExHippoRepository.create(location);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }

            return hippoRepository;
        }

        void closeHippoRepository() {
            if (hippoRepository != null) {
                final String location = hippoRepository.getLocation();
                boolean isRemoteRepository = location != null && (location.startsWith("rmi:") || location.startsWith("http:") || location.startsWith("https:"));

                if (isRemoteRepository) {
                    hippoRepository.close();
                }
            }
        }
    }
}
