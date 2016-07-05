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

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.IF_UNMODIFIED_SINCE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;

/**
 * Request builder which includes a body component
 * 
 * @author bbpennel
 */
public abstract class BodyRequestBuilder extends
        RequestBuilder {

    /**
     * Instantiate builder
     * 
     * @param uri uri request will be issued to
     * @param client the client
     */
    protected BodyRequestBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    /**
     * Add a body to this request from a stream, with application/octet-stream as its content type
     * 
     * @param stream InputStream of the content to be sent to the server
     * @return this builder
     */
    protected BodyRequestBuilder body(final InputStream stream) {
        return body(stream, null);
    }

    /**
     * Add a body to this request as a stream with the given content type
     * 
     * @param stream InputStream of the content to be sent to the server
     * @param contentType the Content-Type of the body
     * @return this builder
     */
    protected BodyRequestBuilder body(final InputStream stream, final String contentType) {
        if (stream != null) {
            String type = contentType;
            if (type == null) {
                type = "application/octet-stream";
            }

            ((HttpEntityEnclosingRequestBase) request).setEntity(new InputStreamEntity(stream));
            request.addHeader(CONTENT_TYPE, type);
        }

        return this;
    }

    /**
     * Add the given file as the body for this request with the provided content type
     * 
     * @param file File containing the content to be sent to the server
     * @param contentType the Content-Type of the body
     * @return this builder
     * @throws IOException when unable to stream the body file
     */
    protected BodyRequestBuilder body(final File file, final String contentType) throws IOException {
        return body(new FileInputStream(file), contentType);
    }

    /**
     * Provide a SHA-1 checksum for the body of this request
     * 
     * @param digest sha-1 checksum to provide as the digest for the request body
     * @return this builder
     */
    protected BodyRequestBuilder digest(final String digest) {
        if (digest != null) {
            request.addHeader(DIGEST, "sha1=" + digest);
        }
        return this;
    }

    /**
     * Provide a if-unmodified-since header for this request
     * 
     * @param modified date to provide as the if-unmodified-since header
     * @return this builder
     */
    public BodyRequestBuilder ifUnmodifiedSince(final String modified) {
        if (modified != null) {
            request.setHeader(IF_UNMODIFIED_SINCE, modified);
        }
        return this;
    }

    /**
     * Provide an etag for the if-match header for this request
     * 
     * @param etag etag to provide as the if-match header
     * @return this builder
     */
    protected BodyRequestBuilder ifMatch(final String etag) {
        if (etag != null) {
            request.setHeader(IF_MATCH, etag);
        }
        return this;
    }
}
