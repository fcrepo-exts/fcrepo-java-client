package org.fcrepo.client;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

public abstract class TransactionBuilder extends RequestBuilder {

    /**
     * Instantiate builder. Throws an IllegalArgumentException if either the uri or client are null.
     *
     * @param uri    uri of the resource this request is being made to
     * @param client the client
     */
    protected TransactionBuilder(URI uri, FcrepoClient client) {
        super(uri, client);
    }

    public static TransactionBuilder commit(final URI uri, final FcrepoClient client) {
        return new TransactionBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.PUT.createRequest(targetUri);
            }
        };
    }

    public static TransactionBuilder keepAlive(final URI uri, final FcrepoClient client) {
        return new TransactionBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.POST.createRequest(targetUri);
            }
        };
    }

    public static TransactionBuilder status(final URI uri, final FcrepoClient client) {
        return new TransactionBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.GET.createRequest(targetUri);
            }
        };
    }

    public static TransactionBuilder rollback(final URI uri, final FcrepoClient client) {
        return new TransactionBuilder(uri, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.DELETE.createRequest(targetUri);
            }
        };
    }

    @Override
    protected TransactionBuilder addHeader(String name, String value) {
        return (TransactionBuilder) super.addHeader(name, value);
    }

}
