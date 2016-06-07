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

import static org.fcrepo.client.FedoraHeaderConstants.ACCEPT;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.IF_NONE_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;
import static org.fcrepo.client.FedoraHeaderConstants.RANGE;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

/**
 * Builds a GET request to retrieve the content of a resource from the Fedora HTTP API
 * 
 * @author bbpennel
 */
public class GetBuilder<T extends GetBuilder<T>> extends
        RequestBuilder<GetBuilder<T>> {

    private static final Logger LOGGER = getLogger(GetBuilder.class);

    protected Long rangeStart;

    protected Long rangeEnd;

    protected String acceptType;

    protected String prefer;

    protected List<URI> includeUris;

    protected List<URI> omitUris;

    protected String etag;

    protected String lastModified;

    /**
     * Construct a GetBuilder
     * 
     * @param uri the target
     * @param client the client for this request
     */
    public GetBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.GET.createRequest(targetUri);
    }

    @Override
    protected void populateRequest(final HttpRequestBase request) {
        if (acceptType != null) {
            request.setHeader(ACCEPT, acceptType);
        }

        // Construct the prefer header, with include and omit parameters if available.
        if (prefer != null) {
            final StringJoiner preferJoin = new StringJoiner("; ");
            preferJoin.add("return=" + prefer);

            if (includeUris != null) {
                final String include = includeUris.stream().map(URI::toString).collect(Collectors.joining(" "));
                if (include.length() > 0) {
                    preferJoin.add("include=\"" + include + "\"");
                }
            }

            if (omitUris != null) {
                final String omit = omitUris.stream().map(URI::toString).collect(Collectors.joining(" "));
                if (omit.length() > 0) {
                    preferJoin.add("omit=\"" + omit + "\"");
                }
            }

            request.setHeader(PREFER, preferJoin.toString());
        }

        // Compute and add the range header
        if (rangeStart != null || rangeEnd != null) {
            String range = "bytes=";
            if (rangeStart != null && rangeStart.longValue() > -1L) {
                range += rangeStart.toString();
            }
            range += "-";
            if (rangeEnd != null && rangeEnd.longValue() > -1L) {
                range += rangeEnd.toString();
            }
            request.setHeader(RANGE, range);
        }

        // Add modification check headers
        if (etag != null) {
            request.setHeader(IF_NONE_MATCH, etag);
        }
        if (lastModified != null) {
            request.setHeader(IF_MODIFIED_SINCE, lastModified);
        }

        LOGGER.debug("Fcrepo GET request headers: {}", (Object[]) request.getAllHeaders());
    }

    /**
     * Add the accept header to this request to negotiate the response format.
     * 
     * @param mediaType media type to set as the accept header. It should be a value from one of the allowed RDF
     *        source formats supported by Fedora.
     * @return this builder
     */
    public GetBuilder<T> accept(final String mediaType) {
        this.acceptType = mediaType;
        return self();
    }

    /**
     * Set the byte range of content to retrieve
     * 
     * @param start beginning byte index
     * @param end ending byte index
     * @return this builder
     */
    public GetBuilder<T> range(final Long start, final Long end) {
        this.rangeStart = start;
        this.rangeEnd = end;
        return self();
    }

    /**
     * Set the prefer header for this request to minimal, to indicate that only triples directly related to a resource
     * should be returned.
     * 
     * @return this builder
     */
    public GetBuilder<T> preferMinimal() {
        this.prefer = "minimal";
        return self();
    }

    /**
     * Set the prefer header for this request to representation, to indicate that links to other resources and their
     * properties should also be included.
     * 
     * @return this builder
     */
    public GetBuilder<T> preferRepresentation() {
        this.prefer = "representation";
        return self();
    }

    /**
     * Set the prefer header for this request to representation, to indicate that links to other resources and their
     * properties should also be included. The set of properties returned can be further specified by providing lists
     * of LDP defined preferences to omit or include.
     * 
     * @param includeUris URIs of LDP defined preferences to include
     * @param omitUris URIs of LDP defined preferences to omit
     * @return this builder
     */
    public GetBuilder<T> preferRepresentation(final List<URI> includeUris, final List<URI> omitUris) {
        this.preferRepresentation();
        this.includeUris = includeUris;
        this.omitUris = omitUris;
        return self();
    }

    /**
     * Provide an etag for the if-none-match header for this request
     * 
     * @param etag etag to provide as the if-none-match header
     * @return this builder
     */
    public GetBuilder<T> ifNoneMatch(final String etag) {
        this.etag = etag;
        return self();
    }

    /**
     * Provide a if-last-modified header for this request
     * 
     * @param lastModified date to provided as the if-modified-since header
     * @return this builder
     */
    public GetBuilder<T> ifModifiedSince(final String lastModified) {
        this.lastModified = lastModified;
        return self();
    }
}
