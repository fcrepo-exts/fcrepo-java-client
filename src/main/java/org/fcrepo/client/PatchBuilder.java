/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
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
    public PatchBuilder body(final InputStream stream) {
        return (PatchBuilder) super.body(stream, SPARQL_UPDATE);
    }

    @Override
    public PatchBuilder ifMatch(final String etag) {
        return (PatchBuilder) super.ifMatch(etag);
    }

    @Override
    public PatchBuilder ifUnmodifiedSince(final String modified) {
        return (PatchBuilder) super.ifUnmodifiedSince(modified);
    }

    @Deprecated
    @Override
    public PatchBuilder digest(final String digest) {
        return (PatchBuilder) super.digest(digest);
    }

    @Override
    public PatchBuilder digest(final String digest, final String alg) {
        return (PatchBuilder) super.digest(digest, alg);
    }

    @Override
    public PatchBuilder digestMd5(final String digest) {
        return (PatchBuilder) super.digestMd5(digest);
    }

    @Override
    public PatchBuilder digestSha1(final String digest) {
        return (PatchBuilder) super.digestSha1(digest);
    }

    @Override
    public PatchBuilder digestSha256(final String digest) {
        return (PatchBuilder) super.digestSha256(digest);
    }
}
