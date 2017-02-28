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
import java.util.StringJoiner;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;

/**
 * Request builder which includes a body component
 * 
 * @author bbpennel
 */
public abstract class BodyRequestBuilder extends
        RequestBuilder {

    private StringJoiner digestJoiner;

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
     * Provide a SHA-1 checksum for the body of this request.
     * 
     * @deprecated Use {@link #digestSha1(java.lang.String)}.
     * @param digest sha-1 checksum to provide as the digest for the request body
     * @return this builder
     */
    @Deprecated
    protected BodyRequestBuilder digest(final String digest) {
        return digestSha1(digest);
    }

    /**
     * Provide a checksum for the body of this request
     * 
     * @param digest checksum to provide as the digest for the request body
     * @param alg abbreviated algorithm identifier for the type of checksum being
     *      added (for example, sha1, md5, etc)
     * @return this builder
     */
    protected BodyRequestBuilder digest(final String digest, final String alg) {
        if (digest != null) {
            if (digestJoiner == null) {
                digestJoiner = new StringJoiner(", ");
            }
            digestJoiner.add(alg + "=" + digest);
            request.setHeader(DIGEST, digestJoiner.toString());
        }
        return this;
    }

    /**
     * Provide a SHA-1 checksum for the body of this request.
     * 
     * @param digest sha-1 checksum to provide as the digest for the request body
     * @return this builder
     */
    protected BodyRequestBuilder digestSha1(final String digest) {
        return digest(digest, "sha1");
    }

    /**
     * Provide a MD5 checksum for the body of this request
     * 
     * @param digest MD5 checksum to provide as the digest for the request body
     * @return this builder
     */
    protected BodyRequestBuilder digestMd5(final String digest) {
        return digest(digest, "md5");
    }

    /**
     * Provide a SHA-256 checksum for the body of this request
     * 
     * @param digest sha-256 checksum to provide as the digest for the request body
     * @return this builder
     */
    protected BodyRequestBuilder digestSha256(final String digest) {
        return digest(digest, "sha256");
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
