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

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.Args;

/**
 * @author bbpennel
 */
public abstract class RequestBuilder<T extends RequestBuilder<T>> {

    // Fedora client which will make this request
    protected FcrepoClient client;

    // URL this request will be executed against
    protected URI targetUri;

    /**
     * Instantiate builder.  Throws an IllegalArgumentException if either the uri or
     * client are null.
     * 
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    protected RequestBuilder(final URI uri, final FcrepoClient client) {
        Args.notNull(uri, "uri");
        Args.notNull(client, "client");

        this.targetUri = uri;
        this.client = client;
    }

    /**
     * Performs the request constructed in this builder and returns the response
     * 
     * @return the repository response
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        final HttpRequestBase request = createRequest();

        populateRequest(request);

        return client.executeRequest(targetUri, request);
    };

    protected abstract HttpRequestBase createRequest();

    protected void populateRequest(final HttpRequestBase request) throws FcrepoOperationFailedException {
    }

    protected abstract T self();
}
