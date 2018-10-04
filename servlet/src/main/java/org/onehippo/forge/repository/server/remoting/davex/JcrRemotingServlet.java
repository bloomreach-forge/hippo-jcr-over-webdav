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
package org.onehippo.forge.repository.server.remoting.davex;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.hippoecm.repository.HippoRepositoryFactory;
import org.onehippo.forge.repository.server.JcrHippoRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>JcrRemotingServlet</code> is an extended version of the
 * {@link org.apache.jackrabbit.server.remoting.davex.JcrRemotingServlet},
 * creating repository instance through HippoRepositoryFactory.
 * <P>
 * This servlet can have 'repository-address' servlet init parameter to configure
 * the repository address. The default repository address is 'vm://' if nothing is configured.
 * </P>
 */
public class JcrRemotingServlet extends org.apache.jackrabbit.server.remoting.davex.JcrRemotingServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(JcrRemotingServlet.class);

    public static final String REPOSITORY_ADDRESS_PARAM = "repository-address";

    public static final String ALLOW_ANONYMOUS_ACCESS_PARAM = "allowAnonymousAccess";

    private String repositoryAddress = "vm://";

    private volatile JcrHippoRepositoryWrapper repository;

    private boolean allowAnonymousAccess;

    @Override
    public void init() throws ServletException {
        super.init();

        String param = getInitParameter(REPOSITORY_ADDRESS_PARAM);

        if (param != null && !"".equals(param.trim())) {
            repositoryAddress = param.trim();
        }

        param = getInitParameter(ALLOW_ANONYMOUS_ACCESS_PARAM);

        if (param != null && !"".equals(param.trim())) {
            allowAnonymousAccess = Boolean.parseBoolean(param);
        }
    }

    @Override
    public void destroy() {
        if (repository != null) {
            repository.closeHippoRepository();
        }

        super.destroy();
    }

    @Override
    protected Repository getRepository() {
        JcrHippoRepositoryWrapper repo = repository;

        if (repo == null) {
            synchronized (this) {
                repo = repository;

                if (repo == null) {
                    try {
                        repo = new JcrHippoRepositoryWrapper(
                                HippoRepositoryFactory.getHippoRepository(repositoryAddress), allowAnonymousAccess);
                        repository = repo;
                    } catch (RepositoryException e) {
                        log.error("Repository is not found.", e);
                    }
                }
            }
        }

        return repo;
    }
}
