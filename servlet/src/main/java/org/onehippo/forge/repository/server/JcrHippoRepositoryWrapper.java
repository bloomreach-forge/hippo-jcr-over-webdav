/**
 * Copyright 2018 Hippo.
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
package org.onehippo.forge.repository.server;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import org.hippoecm.repository.HippoRepository;

/**
 * JCR Repository implementation wrapping HippoRepository.
 */
public class JcrHippoRepositoryWrapper implements Repository {

    private HippoRepository hippoRepository;

    public JcrHippoRepositoryWrapper(HippoRepository hippoRepository) {
        if (hippoRepository == null) {
            throw new IllegalArgumentException("HippoRepository cannot be null!");
        }

        this.hippoRepository = hippoRepository;
    }

    public String getDescriptor(String key) {
        return hippoRepository.getRepository().getDescriptor(key);
    }

    public String[] getDescriptorKeys() {
        return hippoRepository.getRepository().getDescriptorKeys();
    }

    public Session login() throws LoginException, RepositoryException {
        return hippoRepository.login();
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new IllegalArgumentException("Only javax.jcr.SimpleCredentials is supported.");
        }

        return hippoRepository.login((SimpleCredentials) credentials);
    }

    public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login();
    }

    public Session login(Credentials credentials, String workspaceName) throws LoginException,
            NoSuchWorkspaceException, RepositoryException {
        return login(credentials);
    }

    public Value getDescriptorValue(String key) {
        return hippoRepository.getRepository().getDescriptorValue(key);
    }

    public Value[] getDescriptorValues(String key) {
        return hippoRepository.getRepository().getDescriptorValues(key);
    }

    public boolean isSingleValueDescriptor(String key) {
        return hippoRepository.getRepository().isSingleValueDescriptor(key);
    }

    public boolean isStandardDescriptor(String key) {
        return hippoRepository.getRepository().isStandardDescriptor(key);
    }

    public void closeHippoRepository() {
        if (hippoRepository != null) {
            final String location = hippoRepository.getLocation();
            boolean isRemoteRepository = location != null && (location.startsWith("rmi:") || location.startsWith("http:") || location.startsWith("https:"));

            if (isRemoteRepository) {
                hippoRepository.close();
            }
        }
    }
}
