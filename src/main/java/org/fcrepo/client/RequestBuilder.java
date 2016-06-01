/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.client;

import java.net.URI;

/**
 * @author bbpennel
 */
public abstract class RequestBuilder<T extends RequestBuilder<T>> {
    // Fedora client which will make this request
    protected FcrepoClient client;
    // URL this request will be executed against
    protected URI targetUri;

    protected RequestBuilder(URI uri, FcrepoClient client) {
        this.targetUri = uri;
        this.client = client;
    }
    
    /**
     * Performs the request constructed in this builder and returns the response
     * 
     * @return
     */
    public abstract FcrepoResponse perform() throws FcrepoOperationFailedException;

    protected abstract T self();
}
