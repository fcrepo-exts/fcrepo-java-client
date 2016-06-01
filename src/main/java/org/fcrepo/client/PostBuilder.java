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

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.SLUG;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.slf4j.Logger;

/**
 * Builds a post request for interacting with the Fedora HTTP API in order to create a new resource within an LDP
 * container.
 * 
 * @author bbpennel
 */
public class PostBuilder<T extends PostBuilder<T>> extends BodyRequestBuilder<PostBuilder<T>> {

    private static final Logger LOGGER = getLogger(PostBuilder.class);

    protected String digest;

    protected String contentDisposition;

    protected String slug;

    public PostBuilder(URI uri, FcrepoClient client) {
        super(uri, client);
    }

    @Override
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        final HttpMethods method = HttpMethods.POST;
        final HttpEntityEnclosingRequestBase request =
                (HttpEntityEnclosingRequestBase) method.createRequest(targetUri);

        if (bodyStream != null) {
            request.setEntity(new InputStreamEntity(bodyStream));
            request.addHeader(CONTENT_TYPE, contentType);
        }

        if (digest != null) {
            request.addHeader(DIGEST, "sha1=" + digest);
        }

        if (slug != null) {
            request.addHeader(SLUG, slug);
        }

        if (contentDisposition != null) {
            final ContentDisposition cdValue = ContentDisposition.type("attachment")
                    .fileName(contentDisposition)
                    .build();
            request.addHeader(CONTENT_DISPOSITION, cdValue.toString());
        }

        LOGGER.debug("Fcrepo POST request headers: {}", (Object[]) request.getAllHeaders());

        return client.executeRequest(targetUri, request);
    }

    @Override
    protected PostBuilder<T> self() {
        return this;
    }

    /**
     * Provide a SHA-1 checksum for the body of this request
     * 
     * @param value
     * @return
     */
    public PostBuilder<T> digest(String value) {
        this.digest = value;
        return self();
    }

    /**
     * Provide a content disposition header which will be used as the filename
     * 
     * @param value
     * @return
     */
    public PostBuilder<T> contentDisposition(String value) {
        this.contentDisposition = value;
        return self();
    }

    /**
     * Provide a suggested name for the new child resource, which the repository may ignore.
     * 
     * @param value
     * @return
     */
    public PostBuilder<T> slug(String value) {
        this.slug = value;
        return self();
    }

}
