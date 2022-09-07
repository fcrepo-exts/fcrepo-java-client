/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a POST request for creating a memento (LDPRm) from the current state of an LDPRv.
 *
 * @author bbpennel
 */
public class OriginalMementoBuilder extends RequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public OriginalMementoBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.POST.createRequest(targetUri);
    }

    @Override
    public OriginalMementoBuilder addHeader(final String name, final String value) {
        return (OriginalMementoBuilder) super.addHeader(name, value);
    }

    @Override
    public OriginalMementoBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (OriginalMementoBuilder) super.addLinkHeader(linkHeader);
    }

}
