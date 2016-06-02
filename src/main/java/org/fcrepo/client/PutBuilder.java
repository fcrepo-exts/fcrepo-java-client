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
 * Builds a PUT request for interacting with the Fedora HTTP API in order to create a resource with a specified path,
 * or replace the triples associated with a resource with the triples provided in the request body.
 * 
 * @author bbpennel
 */
public class PutBuilder<T extends PutBuilder<T>> extends BodyRequestBuilder<PutBuilder<T>> {

    private static final Logger LOGGER = getLogger(PutBuilder.class);

    protected PutBuilder(URI uri, FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected PutBuilder<T> self() {
        return this;
    }

    @Override
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        final HttpMethods method = HttpMethods.PUT;
        final HttpEntityEnclosingRequestBase request =
                (HttpEntityEnclosingRequestBase) method.createRequest(targetUri);

        addBody(request);
        addDigest(request);
        
        addIfUnmodifiedSince(request);
        addIfMatch(request);

        LOGGER.debug("Fcrepo PUT request headers: {}", (Object[]) request.getAllHeaders());

        return client.executeRequest(targetUri, request);
    }

    /**
     * Provide an etag for the if-match header for this request
     * 
     * @param value
     * @return
     */
    public PutBuilder<T> ifMatch(String value) {
        this.etag = value;
        return self();
    }

    /**
     * Provide a if-unmodified-since header for this request
     * 
     * @param lastModified
     * @return
     */
    public PutBuilder<T> ifUnmodifiedSince(String value) {
        this.unmodifiedSince = value;
        return self();
    }
    
    /**
     * Provide a SHA-1 checksum for the body of this request
     * 
     * @param value
     * @return
     */
    public PutBuilder<T> digest(String value) {
        this.digest = value;
        return self();
    }
}
