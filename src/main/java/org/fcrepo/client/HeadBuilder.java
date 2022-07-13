/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;
import java.time.Instant;

import org.apache.http.client.methods.HttpRequestBase;
import org.fcrepo.client.FcrepoResponse.TransactionURI;

/**
 * Builds a HEAD request to retrieve resource headers.
 *
 * @author bbpennel
 */
public class HeadBuilder extends RetrieveRequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri request will be issued to
     * @param client the client
     */
    public HeadBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
        this.request = HttpMethods.HEAD.createRequest(targetUri);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.HEAD.createRequest(targetUri);
    }

    @Override
    public HeadBuilder disableRedirects() {
        return (HeadBuilder) super.disableRedirects();
    }

    @Override
    public HeadBuilder wantDigest(final String value) {
        return (HeadBuilder) super.wantDigest(value);
    }

    @Override
    public HeadBuilder noCache() {
        return (HeadBuilder) super.noCache();
    }

    @Override
    public HeadBuilder acceptDatetime(final Instant acceptInstant) {
        return (HeadBuilder) super.acceptDatetime(acceptInstant);
    }

    @Override
    public HeadBuilder acceptDatetime(final String acceptDatetime) {
        return (HeadBuilder) super.acceptDatetime(acceptDatetime);
    }

    @Override
    public HeadBuilder addHeader(final String name, final String value) {
        return (HeadBuilder) super.addHeader(name, value);
    }

    @Override
    public HeadBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (HeadBuilder) super.addLinkHeader(linkHeader);
    }

    @Override
    public HeadBuilder addTransaction(final TransactionURI transaction) {
        return (HeadBuilder) super.addTransaction(transaction);
    }
}
