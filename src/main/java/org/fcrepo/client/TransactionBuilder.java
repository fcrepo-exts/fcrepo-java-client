/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.Args;

/**
 * Creates different RequestBuilders for interacting with the Fedora Transaction API.
 *
 * @author mikejritter
 */
public class TransactionBuilder {

    private final URI uri;
    private final FcrepoClient client;

    /**
     * Instantiate builder. Throws an IllegalArgumentException if either the uri or client are null.
     *
     * @param uri    uri of the resource this request is being made to
     * @param client the client
     */
    public TransactionBuilder(final URI uri, final FcrepoClient client) {
        Args.notNull(uri, "uri");
        Args.notNull(client, "client");

        this.uri = uri;
        this.client = client;
    }

    /**
     * Create a RequestBuilder for a commit action
     *
     * @return a commit RequestBuilder
     */
    public RequestBuilder commit() {
        return new RequestBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.PUT.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a KeepAlive action
     *
     * @return a keepalive RequestBuilder
     */
    public RequestBuilder keepAlive() {
        return new RequestBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.POST.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a status action
     *
     * @return a status RequestBuilder
     */
    public RequestBuilder status() {
        return new RequestBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.GET.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a rollback action
     *
     * @return a rollback RequestBuilder
     */
    public RequestBuilder rollback() {
        return new RequestBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.DELETE.createRequest(targetUri);
            }
        };
    }

}
