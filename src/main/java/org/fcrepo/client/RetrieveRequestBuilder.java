/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.fcrepo.client.FedoraHeaderConstants.CACHE_CONTROL;
import static org.fcrepo.client.FedoraHeaderConstants.WANT_DIGEST;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;
import static org.fcrepo.client.FedoraHeaderConstants.ACCEPT_DATETIME;

import java.net.URI;
import java.time.Instant;

import org.apache.http.client.config.RequestConfig;

/**
 * Abstract builder for requests to retrieve information from the server
 *
 * @author bbpennel
 */
public abstract class RetrieveRequestBuilder extends RequestBuilder {

    protected RetrieveRequestBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    /**
     * Disable following redirects.
     *
     * @return this builder
     */
    public RetrieveRequestBuilder disableRedirects() {
        request.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
        return this;
    }

    /**
     * Provide a Want-Digest header for this request
     *
     * @param value header value, following the syntax defined in: https://tools.ietf.org/html/rfc3230#section-4.3.1
     * @return this builder
     */
    public RetrieveRequestBuilder wantDigest(final String value) {
        if (value != null) {
            request.setHeader(WANT_DIGEST, value);
        }
        return this;
    }

    /**
     * Provide a Cache-Control header with value "no-cache"
     *
     * @return this builder
     */
    public RetrieveRequestBuilder noCache() {
        request.setHeader(CACHE_CONTROL, "no-cache");
        return this;
    }

    /**
     * Provide an Accept-Datetime header in RFC1123 format from the given instant for memento datetime negotiation.
     *
     * @param acceptInstant the accept datetime represented as an Instant.
     * @return this builder
     */
    public RetrieveRequestBuilder acceptDatetime(final Instant acceptInstant) {
        if (acceptInstant != null) {
            final String rfc1123Datetime = UTC_RFC_1123_FORMATTER.format(acceptInstant);
            request.setHeader(ACCEPT_DATETIME, rfc1123Datetime);
        }
        return this;
    }

    /**
     * Provide an Accept-Datetime from the given RFC1123 formatted string.
     *
     * @param acceptDatetime the accept datetime as a string, must be in RFC1123 format.
     * @return this builder
     */
    public RetrieveRequestBuilder acceptDatetime(final String acceptDatetime) {
        if (acceptDatetime != null) {
            // Parse the datetime to ensure that it is in RFC1123 format
            UTC_RFC_1123_FORMATTER.parse(acceptDatetime);
            request.setHeader(ACCEPT_DATETIME, acceptDatetime);
        }
        return this;
    }
}
