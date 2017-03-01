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

import static org.fcrepo.client.FedoraHeaderConstants.ACCEPT;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.IF_NONE_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;
import static org.fcrepo.client.FedoraHeaderConstants.RANGE;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.ws.rs.core.EntityTag;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a GET request to retrieve the content of a resource from the Fedora HTTP API
 * 
 * @author bbpennel
 */
public class GetBuilder extends
        RequestBuilder {

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

    /**
     * Add the accept header to this request to negotiate the response format.
     * 
     * @param mediaType media type to set as the accept header. It should be a value from one of the allowed RDF
     *        source formats supported by Fedora.
     * @return this builder
     */
    public GetBuilder accept(final String mediaType) {
        if (mediaType != null) {
            request.setHeader(ACCEPT, mediaType);
        }
        return this;
    }

    /**
     * Set the byte range of content to retrieve
     * 
     * @param rangeStart beginning byte index
     * @param rangeEnd ending byte index
     * @return this builder
     */
    public GetBuilder range(final Long rangeStart, final Long rangeEnd) {
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
        return this;
    }

    /**
     * Set the prefer header for this request to minimal, to indicate that only triples directly related to a resource
     * should be returned.
     * 
     * @return this builder
     */
    public GetBuilder preferMinimal() {
        request.setHeader(PREFER, buildPrefer("minimal", null, null));
        return this;
    }

    /**
     * Disable following redirects.
     *
     * @return this builder
     */
    public GetBuilder disableRedirects() {
        request.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
        return this;
    }

    /**
     * Set the prefer header for this request to representation, to indicate that links to other resources and their
     * properties should also be included.
     * 
     * @return this builder
     */
    public GetBuilder preferRepresentation() {
        request.setHeader(PREFER, buildPrefer("representation", null, null));
        return this;
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
    public GetBuilder preferRepresentation(final List<URI> includeUris, final List<URI> omitUris) {
        request.setHeader(PREFER, buildPrefer("representation", includeUris, omitUris));
        return this;
    }

    private String buildPrefer(final String prefer, final List<URI> includeUris, final List<URI> omitUris) {
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

        return preferJoin.toString();
    }

    /**
     * Provide an etag for the if-none-match header for this request
     * 
     * @param etag etag to provide as the if-none-match header
     * @return this builder
     */
    public GetBuilder ifNoneMatch(final String etag) {
        if (etag != null) {
            request.setHeader(IF_NONE_MATCH, etag);
        }
        return this;
    }

    /**
     * Provide an etag for the if-none-match header for this request
     * 
     * @param etag EntityTag to provide as the if-none-match header
     * @return this builder
     */
    public GetBuilder ifNoneMatch(final EntityTag etag) {
        if (etag != null) {
            request.setHeader(IF_NONE_MATCH, "\"" + etag.getValue() + "\"");
        }
        return this;
    }

    /**
     * Provide a if-last-modified header for this request
     * 
     * @param lastModified date to provided as the if-modified-since header
     * @return this builder
     */
    public GetBuilder ifModifiedSince(final String lastModified) {
        if (lastModified != null) {
            request.setHeader(IF_MODIFIED_SINCE, lastModified);
        }
        return this;
    }
}
