/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * A Transaction aware client which adds the Atomic_ID header to requests
 *
 * @author mikejritter
 */
public class TransactionalFcrepoClient extends FcrepoClient {

    private final FcrepoResponse.TransactionURI transactionURI;

    /**
     *
     * @param transactionURI the transaction to append to all requests
     * @param httpClientBuilder the httpclient
     * @param throwExceptionOnFailure whether to throw an exception on any non-2xx or 3xx HTTP responses
     */
    public TransactionalFcrepoClient(final FcrepoResponse.TransactionURI transactionURI,
                                     final FcrepoHttpClientBuilder httpClientBuilder,
                                     final Boolean throwExceptionOnFailure) {
        super(httpClientBuilder, throwExceptionOnFailure);

        if (transactionURI == null) {
            throw new IllegalArgumentException("TransactionURI cannot be null");
        }
        this.transactionURI = transactionURI;
    }

    public FcrepoResponse.TransactionURI getTransactionURI() {
        return transactionURI;
    }

    /**
     * Commit a transaction by performing a PUT
     *
     * @return the commit RequestBuilder
     */
    public RequestBuilder transactionCommit() {
        return new RequestBuilder(transactionURI.get(), this) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.PUT.createRequest(targetUri);
            }
        };
    }

    /**
     * Retrieve the status of a transaction by performing a GET
     *
     * @return the status RequestBuilder
     */
    public RequestBuilder transactionStatus() {
        return new RequestBuilder(transactionURI.get(), this) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.GET.createRequest(targetUri);
            }
        };
    }

    /**
     * Keep a transaction alive by performing a POST
     *
     * @return the keep alive RequestBuilder
     */
    public RequestBuilder transactionKeepAlive() {
        return new RequestBuilder(transactionURI.get(), this) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.POST.createRequest(targetUri);
            }
        };
    }

    /**
     * Rollback a transaction by performing a DELETE
     *
     * @return the rollback RequestBuilder
     */
    public RequestBuilder transactionRollback() {
        return new RequestBuilder(transactionURI.get(), this) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.DELETE.createRequest(targetUri);
            }
        };
    }

    @Override
    public GetBuilder get(final URI url) {
        final var builder = super.get(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

    @Override
    public HeadBuilder head(final URI url) {
        final var builder = super.head(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

    @Override
    public DeleteBuilder delete(final URI url) {
        final var builder = super.delete(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

    @Override
    public OptionsBuilder options(final URI url) {
        final var builder = super.options(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

    @Override
    public PatchBuilder patch(final URI url) {
        final var builder = super.patch(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

    @Override
    public PostBuilder post(final URI url) {
        final var builder = super.post(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

    @Override
    public PutBuilder put(final URI url) {
        final var builder = super.put(url);
        builder.addTransaction(transactionURI);
        return builder;
    }

}
