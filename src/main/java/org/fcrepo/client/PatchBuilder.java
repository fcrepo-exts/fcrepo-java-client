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

import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a PUT request for interacting with the Fedora HTTP API in order to modify the triples associated with a
 * resource with SPARQL-Update.
 * 
 * @author bbpennel
 */
public class PatchBuilder extends BodyRequestBuilder {

    private static final String SPARQL_UPDATE = "application/sparql-update";

    /**
     * Instantiate builder
     * 
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public PatchBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.PATCH.createRequest(targetUri);
    }

    /**
     * Patch defaults to a sparql update
     */
    @Override
    public BodyRequestBuilder body(final InputStream stream) {
        return super.body(stream, SPARQL_UPDATE);
    }

    /**
     * Provide an etag for the if-match header for this request
     * 
     * @param etag etag to provide as the if-match header
     * @return this builder
     */
    public PatchBuilder ifMatch(final String etag) {
        addIfMatch(etag);
        return this;
    }

    /**
     * Provide a if-unmodified-since header for this request
     * 
     * @param modified date to provide as the if-unmodified-since header
     * @return this builder
     */
    public PatchBuilder ifUnmodifiedSince(final String modified) {
        addIfUnmodifiedSince(modified);
        return this;
    }
}
