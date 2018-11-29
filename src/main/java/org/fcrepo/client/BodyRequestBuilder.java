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

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.IF_STATE_TOKEN;
import static org.fcrepo.client.FedoraHeaderConstants.IF_UNMODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.LinkHeaderConstants.ACL_REL;
import static org.fcrepo.client.LinkHeaderConstants.TYPE_REL;
import static org.fcrepo.client.LinkHeaderConstants.EXTERNAL_CONTENT_HANDLING;
import static org.fcrepo.client.LinkHeaderConstants.EXTERNAL_CONTENT_REL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.fcrepo.client.FcrepoLink.Builder;

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
     * Add the given URI to the request as the location a Non-RDF Source binary should use for external content. The
     * handling parameter must be supplied, and informs the server of how to process the request.
     *
     * @param contentURI URI of the external content.
     * @param contentType Mimetype to supply for the external content.
     * @param handling Name of the handling method, used by the server to determine how to process the external
     *        content URI. Standard values can be found in {@link ExternalContentHandling}.
     * @return this builder
     */
    protected BodyRequestBuilder externalContent(final URI contentURI, final String contentType,
            final String handling) {
        final Builder linkBuilder = FcrepoLink.fromUri(contentURI)
            .rel(EXTERNAL_CONTENT_REL)
            .param(EXTERNAL_CONTENT_HANDLING, handling);

        if (StringUtils.isNotBlank(contentType)) {
            linkBuilder.type(contentType);
        }

        request.addHeader(LINK, linkBuilder.build().toString());
        return this;
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
     * Add an interaction model to the request
     *
     * @param interactionModelUri URI of the interaction model
     * @return this builder
     */
    protected BodyRequestBuilder addInteractionModel(final String interactionModelUri) {
        if (interactionModelUri != null) {
            final FcrepoLink link = FcrepoLink.fromUri(interactionModelUri)
                    .rel(TYPE_REL)
                    .build();
            request.addHeader(LINK, link.toString());
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

    /**
     * Provide the URI to an ACL for this request
     *
     * @param aclUri URI to the ACL
     * @return this builder
     */
    protected BodyRequestBuilder linkAcl(final String aclUri) {
        if (aclUri != null) {
            final FcrepoLink link = FcrepoLink.fromUri(aclUri)
                    .rel(ACL_REL)
                    .build();
            request.addHeader(LINK, link.toString());
        }
        return this;
    }

    /**
     * Provide a value for the if-state-token header for this request.
     *
     * @param token state token value
     * @return this builder
     */
    protected BodyRequestBuilder ifStateToken(final String token) {
        if (token != null) {
            request.setHeader(IF_STATE_TOKEN, token);
        }
        return this;
    }
}
