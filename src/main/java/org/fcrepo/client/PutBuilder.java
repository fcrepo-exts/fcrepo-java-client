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

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a PUT request for interacting with the Fedora HTTP API in order to create a resource with a specified path,
 * or replace the triples associated with a resource with the triples provided in the request body.
 *
 * @author bbpennel
 */
public class PutBuilder extends BodyRequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public PutBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.PUT.createRequest(targetUri);
    }

    @Override
    public PutBuilder body(final InputStream stream, final String contentType) {
        return (PutBuilder) super.body(stream, contentType);
    }

    @Override
    public PutBuilder body(final File file, final String contentType) throws IOException {
        return (PutBuilder) super.body(file, contentType);
    }

    @Override
    public PutBuilder externalContent(final URI contentURI, final String contentType, final String handling) {
        return (PutBuilder) super.externalContent(contentURI, contentType, handling);
    }

    @Override
    public PutBuilder body(final InputStream stream) {
        return (PutBuilder) super.body(stream);
    }

    @Override
    public PutBuilder ifMatch(final String etag) {
        return (PutBuilder) super.ifMatch(etag);
    }

    @Override
    public PutBuilder ifUnmodifiedSince(final String modified) {
        return (PutBuilder) super.ifUnmodifiedSince(modified);
    }

    @Override
    public PutBuilder ifStateToken(final String token) {
        return (PutBuilder) super.ifStateToken(token);
    }

    @Deprecated
    @Override
    public PutBuilder digest(final String digest) {
        return (PutBuilder) super.digest(digest);
    }

    @Override
    public PutBuilder digest(final String digest, final String alg) {
        return (PutBuilder) super.digest(digest, alg);
    }

    @Override
    public PutBuilder digestMd5(final String digest) {
        return (PutBuilder) super.digestMd5(digest);
    }

    @Override
    public PutBuilder digestSha1(final String digest) {
        return (PutBuilder) super.digestSha1(digest);
    }

    @Override
    public PutBuilder digestSha256(final String digest) {
        return (PutBuilder) super.digestSha256(digest);
    }

    @Override
    public PutBuilder addInteractionModel(final String interactionModelUri) {
        return (PutBuilder) super.addInteractionModel(interactionModelUri);
    }

    @Override
    protected PutBuilder linkAcl(final String aclUri) {
        return (PutBuilder) super.linkAcl(aclUri);
    }

    /**
     * Provide a content disposition header which will be used as the filename
     *
     * @param filename the name of the file being provided in the body of the request
     * @return this builder
     * @throws FcrepoOperationFailedException if unable to encode filename
     */
    public PutBuilder filename(final String filename) throws FcrepoOperationFailedException {
        try {
            final String f = (filename != null) ? "; filename=\"" + URLEncoder.encode(filename, "utf-8") + "\"" : "";
            request.addHeader(CONTENT_DISPOSITION, "attachment" + f);
        } catch (final UnsupportedEncodingException e) {
            throw new FcrepoOperationFailedException(request.getURI(), -1, e.getMessage());
        }
        return this;
    }

    /**
     * Set the prefer header for this request to lenient handling, to indicate that server-managed triples will not
     * be included in the request body.
     *
     * @return this builder
     */
    public PutBuilder preferLenient() {
        request.setHeader(PREFER, "handling=lenient; received=\"minimal\"");
        return this;
    }
}
