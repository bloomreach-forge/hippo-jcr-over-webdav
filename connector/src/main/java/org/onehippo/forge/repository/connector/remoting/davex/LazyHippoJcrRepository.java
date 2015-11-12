/**
 * Copyright 2012 Hippo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.repository.connector.remoting.davex;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.transaction.NotSupportedException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.api.ReferenceWorkspace;
import org.hippoecm.repository.api.RepositoryMap;
import org.onehippo.repository.bootstrap.InitializationProcessor;

/**
 * JCR Repository implementation wrapping HippoRepository.
 */
public class LazyHippoJcrRepository implements HippoJcrRepository {

    private String location;
    private HippoRepository hippoRepository;

    public LazyHippoJcrRepository(String location) {
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

    @Override
    public String getDescriptor(String key) {
        return getHippoRepository().getRepository().getDescriptor(key);
    }

    @Override
    public String[] getDescriptorKeys() {
        return getHippoRepository().getRepository().getDescriptorKeys();
    }

    @Override
    public Session login() throws LoginException, RepositoryException {
        return getHippoRepository().login();
    }

    @Override
    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new IllegalArgumentException("Only javax.jcr.SimpleCredentials is supported.");
        }

        return getHippoRepository().login((SimpleCredentials) credentials);
    }

    @Override
    public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login();
    }

    @Override
    public Session login(Credentials credentials, String workspaceName) throws LoginException,
            NoSuchWorkspaceException, RepositoryException {
        return login(credentials);
    }

    @Override
    public Value getDescriptorValue(String key) {
        return getHippoRepository().getRepository().getDescriptorValue(key);
    }

    @Override
    public Value[] getDescriptorValues(String key) {
        return getHippoRepository().getRepository().getDescriptorValues(key);
    }

    @Override
    public boolean isSingleValueDescriptor(String key) {
        return getHippoRepository().getRepository().isSingleValueDescriptor(key);
    }

    @Override
    public boolean isStandardDescriptor(String key) {
        return getHippoRepository().getRepository().isStandardDescriptor(key);
    }

    /* Implementing HippoRepository */

    @Override
    public Session login(String username, char[] password) throws LoginException, RepositoryException {
        return getHippoRepository().login(username, password);
    }

    @Override
    public Session login(SimpleCredentials credentials) throws LoginException, RepositoryException {
        return getHippoRepository().login(credentials);
    }

    @Override
    public void close() {
        getHippoRepository().close();
    }

    @Override
    public UserTransaction getUserTransaction(Session session) throws RepositoryException, NotSupportedException {
        return getHippoRepository().getUserTransaction(session);
    }

    @Override
    public UserTransaction getUserTransaction(TransactionManager tm, Session session) throws NotSupportedException {
        return getHippoRepository().getUserTransaction(tm, session);
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Repository getRepository() {
        return getHippoRepository().getRepository();
    }

    @Override
    public RepositoryMap getRepositoryMap(Node node) throws RepositoryException {
        return getHippoRepository().getRepositoryMap(node);
    }

    @Override
    public InitializationProcessor getInitializationProcessor() {
        return getHippoRepository().getInitializationProcessor();
    }

    @Override
    public ReferenceWorkspace getOrCreateReferenceWorkspace() throws RepositoryException {
        return getHippoRepository().getOrCreateReferenceWorkspace();
    }
}
