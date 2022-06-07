/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

import javax.swing.text.html.Option;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds an OPTIONS request to output information about the supported HTTP methods, etc.
 *
 * @author bbpennel
 */
public class OptionsBuilder extends RequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public OptionsBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.OPTIONS.createRequest(targetUri);
    }

    @Override
    public OptionsBuilder addHeader(final String name, final String value) {
        return (OptionsBuilder) super.addHeader(name, value);
    }

    @Override
    public OptionsBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (OptionsBuilder) super.addLinkHeader(linkHeader);
    }

    @Override
    public OptionsBuilder addTransaction(String transaction) {
        return (OptionsBuilder) super.addTransaction(transaction);
    }
}
