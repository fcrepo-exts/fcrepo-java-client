/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.Args;
import org.fcrepo.client.FcrepoResponse.TransactionURI;

/**
 * Creates different RequestBuilders for interacting with the Fedora Transaction API.
 *
 * @author mikejritter
 */
public class TransactionBuilder {

    public static final String TRANSACTION_ENDPOINT = "fcr:tx";

    private final FcrepoClient client;

    /**
     * Instantiate builder. Throws an IllegalArgumentException if either the uri or client are null.
     *
     * uri    the transaction this request is being made to: either the repository root,
     *               the create transaction endpoint, or a transaction uri
     * @param client the client
     */
    public TransactionBuilder(final FcrepoClient client) {
        Args.notNull(client, "client");

        this.client = client;
    }

    /**
     * Execute {@link TransactionBuilder#start} and return a new {@link TransactionalFcrepoClient} using the Atomic-Id
     * from the response
     *
     * @param uri the uri to start the transaction with
     * @return a Transactional client
     * @throws FcrepoOperationFailedException If the underlying HTTP request results in an error
     * @throws IOException If there is an error closing the underlying HTTP response stream
     */
    public TransactionalFcrepoClient startTransactionalClient(final URI uri)
        throws FcrepoOperationFailedException, IOException {
        try (final FcrepoResponse response = start(uri).perform()) {
            return client.transactionalClient(response.getTransactionUri());
        }
    }

    /**
     * Create a RequestBuilder to start a transaction
     *
     * @param uri Either the base rest endpoint or the transaction endpoint
     * @return the RequestBuilder
     */
    public RequestBuilder start(final URI uri) {
        final URI target;
        final var asString = uri.toString();
        final var txPattern = Pattern.compile("rest/" + TRANSACTION_ENDPOINT + "/?$");
        if (txPattern.matcher(asString).find()) {
            target = uri;
        } else {
            // handle trailing slash in the given uri
            checkRootUri(uri);
            target = URI.create(asString.replaceFirst("/?$", "/" + TRANSACTION_ENDPOINT));
        }

        return new RequestBuilder(target, client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.POST.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a commit action
     *
     * @param txURI the transaction uri
     * @return a commit RequestBuilder
     */
    public RequestBuilder commit(final TransactionURI txURI) {
        return new RequestBuilder(txURI.get(), client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.PUT.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a KeepAlive action
     *
     * @param txURI the transaction uri
     * @return a keepalive RequestBuilder
     */
    public RequestBuilder keepAlive(final TransactionURI txURI) {
        return new RequestBuilder(txURI.get(), client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.POST.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a status action
     *
     * @param txURI the transaction uri
     * @return a status RequestBuilder
     */
    public RequestBuilder status(final TransactionURI txURI) {
        return new RequestBuilder(txURI.get(), client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.GET.createRequest(targetUri);
            }
        };
    }

    /**
     * Create a RequestBuilder for a rollback action
     *
     * @param txURI the transaction uri
     * @return a rollback RequestBuilder
     */
    public RequestBuilder rollback(final TransactionURI txURI) {
        return new RequestBuilder(txURI.get(), client) {
            @Override
            protected HttpRequestBase createRequest() {
                return HttpMethods.DELETE.createRequest(targetUri);
            }
        };
    }

    /**
     * Check that the uri is the root of the fedora rest api
     *
     * @param uri the uri to validate
     */
    private void checkRootUri(final URI uri) {
        final var restPattern = Pattern.compile("rest/?$");
        if (!restPattern.matcher(uri.toString()).find()) {
            throw new IllegalArgumentException("Uri is not a valid transaction endpoint");
        }
    }

}
