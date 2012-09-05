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

import javax.jcr.Repository;

import org.hippoecm.repository.HippoRepository;

/**
 * Merged interface from <code>javax.jcr.Repository</code> and <code>org.hippoecm.repository.HippoRepository</code>
 * for convenience. You can use both interface by defining only one JNDI resource by using
 * this type and <code>org.onehippo.forge.repository.connector.remoting.davex.DavExHippoRepositoryBasedHippoJcrRepositoryFactory</code>.
 */
public interface HippoJcrRepository extends Repository, HippoRepository {

}
