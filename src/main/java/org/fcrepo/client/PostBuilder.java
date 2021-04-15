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
import static org.fcrepo.client.FedoraHeaderConstants.SLUG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.ContentDisposition;

/**
 * Builds a post request for interacting with the Fedora HTTP API in order to create a new resource within an LDP
 * container.
 *
 * @author bbpennel
 */
public class PostBuilder extends BodyRequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public PostBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.POST.createRequest(targetUri);
    }

    @Override
    public PostBuilder body(final InputStream stream, final String contentType) {
        return (PostBuilder) super.body(stream, contentType);
    }

    @Override
    public PostBuilder body(final File file, final String contentType) throws IOException {
        return (PostBuilder) super.body(file, contentType);
    }

    @Override
    public PostBuilder body(final InputStream stream) {
        return (PostBuilder) super.body(stream);
    }

    @Override
    public PostBuilder externalContent(final URI contentURI, final String contentType, final String handling) {
        return (PostBuilder) super.externalContent(contentURI, contentType, handling);
    }

    @Deprecated
    @Override
    public PostBuilder digest(final String digest) {
        return (PostBuilder) super.digest(digest);
    }

    @Override
    public PostBuilder digest(final String digest, final String alg) {
        return (PostBuilder) super.digest(digest, alg);
    }

    @Override
    public PostBuilder digestMd5(final String digest) {
        return (PostBuilder) super.digestMd5(digest);
    }

    @Override
    public PostBuilder digestSha1(final String digest) {
        return (PostBuilder) super.digestSha1(digest);
    }

    @Override
    public PostBuilder digestSha256(final String digest) {
        return (PostBuilder) super.digestSha256(digest);
    }

    @Override
    public PostBuilder addInteractionModel(final String interactionModelUri) {
        return (PostBuilder) super.addInteractionModel(interactionModelUri);
    }

    @Override
    public PostBuilder linkAcl(final String aclUri) {
        return (PostBuilder) super.linkAcl(aclUri);
    }

    @Override
    public PostBuilder addHeader(final String name, final String value) {
        return (PostBuilder) super.addHeader(name, value);
    }

    @Override
    public PostBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (PostBuilder) super.addLinkHeader(linkHeader);
    }

    /**
     * Provide a content disposition header which will be used as the filename
     *
     * @param filename the name of the file being provided in the body of the request
     * @return this builder
     * @throws FcrepoOperationFailedException if unable to encode filename
     */
    public PostBuilder filename(final String filename) throws FcrepoOperationFailedException {
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename)
                .build();
        request.addHeader(CONTENT_DISPOSITION, contentDisposition.toString());
        return this;
    }

    /**
     * Provide a suggested name for the new child resource, which the repository may ignore.
     *
     * @param slug value to supply as the slug header
     * @return this builder
     */
    public PostBuilder slug(final String slug) {
        if (slug != null) {
            request.addHeader(SLUG, slug);
        }
        return this;
    }
}
