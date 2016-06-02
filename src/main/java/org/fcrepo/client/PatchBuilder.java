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

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.slf4j.Logger;

/**
 * Builds a PUT request for interacting with the Fedora HTTP API in order to modify the triples associated with a
 * resource with SPARQL-Update.
 * 
 * @author bbpennel
 */
public class PatchBuilder<T extends PatchBuilder<T>> extends BodyRequestBuilder<PatchBuilder<T>> {

    private static final Logger LOGGER = getLogger(PatchBuilder.class);

    protected PatchBuilder(URI uri, FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected PatchBuilder<T> self() {
        return this;
    }

    @Override
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        final HttpMethods method = HttpMethods.PATCH;
        final HttpEntityEnclosingRequestBase request =
                (HttpEntityEnclosingRequestBase) method.createRequest(targetUri);

        addBody(request);

        addIfUnmodifiedSince(request);
        addIfMatch(request);

        LOGGER.debug("Fcrepo PATCH request headers: {}", (Object[]) request.getAllHeaders());

        return client.executeRequest(targetUri, request);
    }

    /**
     * Provide an etag for the if-match header for this request
     * 
     * @param value
     * @return
     */
    public PatchBuilder<T> ifMatch(String value) {
        this.etag = value;
        return self();
    }

    /**
     * Provide a if-unmodified-since header for this request
     * 
     * @param lastModified
     * @return
     */
    public PatchBuilder<T> ifUnmodifiedSince(String value) {
        this.unmodifiedSince = value;
        return self();
    }
}
