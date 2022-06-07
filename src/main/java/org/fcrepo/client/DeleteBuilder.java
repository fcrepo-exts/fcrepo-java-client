/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a request to delete a resource
 *
 * @author bbpennel
 */
public class DeleteBuilder extends RequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri request will be issued to
     * @param client the client
     */
    public DeleteBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.DELETE.createRequest(targetUri);
    }

    @Override
    public DeleteBuilder addHeader(final String name, final String value) {
        return (DeleteBuilder) super.addHeader(name, value);
    }

    @Override
    public DeleteBuilder addTransaction(String transaction) {
        return (DeleteBuilder) super.addTransaction(transaction);
    }

}
