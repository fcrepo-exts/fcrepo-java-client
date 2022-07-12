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

/**
 * Creates different RequestBuilders for interacting with the Fedora Transaction API.
 *
 * @author mikejritter
 */
public class TransactionBuilder {

    public static final String TRANSACTION_ENDPOINT = "fcr:tx";

    private final URI uri;
    private final FcrepoClient client;

    /**
     * Instantiate builder. Throws an IllegalArgumentException if either the uri or client are null.
     *
     * @param uri    the transaction this request is being made to: either the repository root,
     *               the create transaction endpoint, or a transaction uri
     * @param client the client
     */
    public TransactionBuilder(final URI uri, final FcrepoClient client) {
        Args.notNull(uri, "uri");
        Args.notNull(client, "client");

        this.uri = uri;
        this.client = client;
    }

    /**
     * Create a RequestBuilder to start a transaction
     *
     * @return the RequestBuilder
     */
    public RequestBuilder start() {
        final URI target;
        final var asString = uri.toString();
        final var txPattern = Pattern.compile(TRANSACTION_ENDPOINT + "/?$");
        if (txPattern.matcher(asString).find()) {
            checkTxUri(uri);
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
     * @return a commit RequestBuilder
     */
    public RequestBuilder commit() {
        checkTxUri(uri);
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
        checkTxUri(uri);
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
        checkTxUri(uri);
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
        checkTxUri(uri);
        return new RequestBuilder(uri, client) {
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
     */
    private void checkTxUri(final URI uri) {
        // regex taken from TransactionProvider in fcrepo
        final var transactionPattern = "rest/" + TRANSACTION_ENDPOINT + "(/?|/[0-9a-f\\-]+)$";
        final var pattern = Pattern.compile(transactionPattern);
        if (!pattern.matcher(uri.toString()).find()) {
            throw new IllegalArgumentException("Uri is not a valid transaction endpoint");
        }
    }

}
