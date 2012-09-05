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

import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
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
import javax.transaction.NotSupportedException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.SessionStateThresholdEnum;
import org.hippoecm.repository.api.RepositoryMap;
import org.hippoecm.repository.api.ValueMap;

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

    /**
     * JCR Repository implementation wrapping HippoRepository.
     */
    private static class LazyHippoJcrRepository implements HippoJcrRepository {

        private String location;
        private HippoRepository hippoRepository;

        private LazyHippoJcrRepository(String location) {
            if (location == null || "".equals(location.trim())) {
                throw new IllegalArgumentException("Invalid hippo repository location: " + location);
            }

            this.location = location;
        }

        /*
         * Lazily HippoRepository creating method
         */
        private HippoRepository getHippoRepository() {
            if (hippoRepository == null) {
                try {
                    hippoRepository = DavExHippoRepository.create(location);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }

            return hippoRepository;
        }

        /* Implementing JCR Repository */

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

        /* Implementing HippoRepository */

        public Session login(String username, char[] password) throws LoginException, RepositoryException {
            return getHippoRepository().login(username, password);
        }

        public Session login(SimpleCredentials credentials) throws LoginException, RepositoryException {
            return getHippoRepository().login(credentials);
        }

        public void close() {
            getHippoRepository().close();
        }

        public UserTransaction getUserTransaction(Session session) throws RepositoryException, NotSupportedException {
            return getHippoRepository().getUserTransaction(session);
        }

        public UserTransaction getUserTransaction(TransactionManager tm, Session session) throws NotSupportedException {
            return getHippoRepository().getUserTransaction(tm, session);
        }

        public String getLocation() {
            return location;
        }

        public Repository getRepository() {
            return getHippoRepository().getRepository();
        }

        public RepositoryMap getRepositoryMap(Node node) throws RepositoryException {
            return getHippoRepository().getRepositoryMap(node);
        }

        public ValueMap getValueMap(Node node) throws RepositoryException {
            return getHippoRepository().getValueMap(node);
        }

        public boolean stateThresholdExceeded(Session session, EnumSet<SessionStateThresholdEnum> interests) {
            return getHippoRepository().stateThresholdExceeded(session, interests);
        }
    }
}
