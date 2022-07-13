/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

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

    // private final URI uri;
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

        // this.uri = uri;
        this.client = client;
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

    /**
     * Check that a given uri is a valid transaction url (/rest/fcr:tx or /rest/fcr:tx/uuid)
     *
     * @param uri the uri to validate
     * @throws IllegalArgumentException if the uri is not a transaction endpoint
    private void checkTxUri(final URI uri) {
        // regex taken from TransactionProvider in fcrepo
        final var transactionPattern = "rest/" + TRANSACTION_ENDPOINT + "(/?|/[0-9a-f\\-]+)$";
        final var pattern = Pattern.compile(transactionPattern);
        if (!pattern.matcher(uri.toString()).find()) {
            throw new IllegalArgumentException("Uri is not a valid transaction endpoint");
        }
    }
     */

}
