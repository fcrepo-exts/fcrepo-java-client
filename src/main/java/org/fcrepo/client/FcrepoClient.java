/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

/**
 * Represents a client to interact with Fedora's HTTP API.
 * <p>
 * Users of the {@code FcrepoClient} are responsible for managing connection resources. Specifically, the underlying
 * HTTP connections of this client must be freed. Suggested usage is to create the {@code FcrepoResponse} within a
 * {@code try-with-resources} block, insuring that any resources held by the response are freed automatically.
 * </p>
 * <pre>
 * FcrepoClient client = ...;
 * try (FcrepoResponse res = client.get(...).perform()) {
 *     // do something with the response
 * } catch (FcrepoOperationFailedException|IOException e) {
 *     // handle any exceptions
 * }
 * </pre>
 *
 * @author Aaron Coburn
 * @since October 20, 2014
 */
public class FcrepoClient implements Closeable {

    private CloseableHttpClient httpclient;
    private FcrepoHttpClientBuilder httpClientBuilder;

    private Boolean throwExceptionOnFailure = true;

    private static final Logger LOGGER = getLogger(FcrepoClient.class);

    /**
     * Build a FcrepoClient
     *
     * @return a client builder
     */
    public static FcrepoClientBuilder client() {
        return new FcrepoClientBuilder();
    }

    /**
     * Create a FcrepoClient with a set of authentication values.
     *
     * @param username the username for the repository
     * @param password the password for the repository
     * @param host the authentication hostname (realm) for the repository
     * @param throwExceptionOnFailure whether to throw an exception on any non-2xx or 3xx HTTP responses
     */
    protected FcrepoClient(final String username, final String password, final String host,
            final Boolean throwExceptionOnFailure) {
        this(new FcrepoHttpClientBuilder(username, password, host), throwExceptionOnFailure);
    }

    /**
     * Create a FcrepoClient which uses the given {@link FcrepoHttpClientBuilder} to manage its http client.
     * FcrepoClient will close the httpClient when {@link #close()} is called.
     * @param httpClientBuilder http client builder to use to connect to the repository
     * @param throwExceptionOnFailure whether to throw an exception on any non-2xx or 3xx HTTP responses
     */
    protected FcrepoClient(final FcrepoHttpClientBuilder httpClientBuilder, final Boolean throwExceptionOnFailure) {
        this.throwExceptionOnFailure = throwExceptionOnFailure;
        this.httpclient = httpClientBuilder.build();
        this.httpClientBuilder = httpClientBuilder;
    }

    /**
     * Create a FcrepoClient which uses the given {@link org.apache.http.impl.client.CloseableHttpClient}.
     * FcrepoClient will close the httpClient when {@link #close()} is called.
     *
     * @param httpClient http client to use to connect to the repository
     * @param throwExceptionOnFailure whether to throw an exception on any non-2xx or 3xx HTTP responses
     */
    protected FcrepoClient(final CloseableHttpClient httpClient, final Boolean throwExceptionOnFailure) {
        this.throwExceptionOnFailure = throwExceptionOnFailure;
        this.httpclient = httpClient;
    }

    /**
     * Make a PUT request to create a resource with a specified path, or replace the triples associated with a
     * resource with the triples provided in the request body.
     *
     * @param url the URL of the resource to which to PUT
     * @return a put request builder object
     */
    public PutBuilder put(final URI url) {
        return new PutBuilder(url, this);
    }

    /**
     * Make a PATCH request to modify the triples associated with a resource with SPARQL-Update.
     *
     * @param url the URL of the resource to which to PATCH
     * @return a patch request builder object
     */
    public PatchBuilder patch(final URI url) {
        return new PatchBuilder(url, this);
    }

    /**
     * Make a POST request to create a new resource within an LDP container.
     *
     * @param url the URL of the resource to which to POST
     * @return a post request builder object
     */
    public PostBuilder post(final URI url) {
        return new PostBuilder(url, this);
    }

    /**
     * Make a POST request to create a new memento (LDPRm) within an LDPCv of the current version of a resource.
     *
     * @param url the URL of the LDPCv in which to create the LDPRm.
     * @return a memento creation request builder object
     */
    public OriginalMementoBuilder createMemento(final URI url) {
        return new OriginalMementoBuilder(url, this);
    }

    /**
     * Make a POST request to create a new memento (LDPRm) within an LDPCv using the given memento-datetime and the
     * request body.
     *
     * @param url the URL of the LDPCv in which to create the LDPRm.
     * @param mementoInstant the memento datetime as an Instant.
     * @return a memento creation request builder object
     */
    public HistoricMementoBuilder createMemento(final URI url, final Instant mementoInstant) {
        return new HistoricMementoBuilder(url, this, mementoInstant);
    }

    /**
     * Make a POST request to create a new memento (LDPRm) within an LDPCv using the given memento-datetime and the
     * request body.
     *
     * @param url the URL of the LDPCv in which to create the LDPRm.
     * @param mementoDatetime the RFC1123 formatted memento datetime.
     * @return a memento creation request builder object
     */
    public HistoricMementoBuilder createMemento(final URI url, final String mementoDatetime) {
        return new HistoricMementoBuilder(url, this, mementoDatetime);
    }

    /**
     * Interact with the Transaction API - start, commit, etc
     *
     * @return a transaction request builder object
     */
    public TransactionBuilder transaction() {
        return new TransactionBuilder(this);
    }

    /**
     * Create a new {@link TransactionalFcrepoClient} which adds the
     * {@link org.fcrepo.client.FcrepoResponse.TransactionURI} to each request
     *
     * @param uri the Transaction to add to each request
     * @return a TransactionFcrepoClient
     */
    public TransactionalFcrepoClient transactionalClient(final FcrepoResponse.TransactionURI uri) {
        return new TransactionalFcrepoClient(uri, httpClientBuilder, throwExceptionOnFailure);
    }

    /**
     * Make a DELETE request to delete a resource
     *
     * @param url the URL of the resource to which to DELETE
     * @return a delete request builder object
     */
    public DeleteBuilder delete(final URI url) {
        return new DeleteBuilder(url, this);
    }

    /**
     * Make a GET request to retrieve the content of a resource
     *
     * @param url the URL of the resource to which to GET
     * @return a get request builder object
     */
    public GetBuilder get(final URI url) {
        return new GetBuilder(url, this);
    }

    /**
     * Make a HEAD request to retrieve resource headers.
     *
     * @param url the URL of the resource to make the HEAD request on.
     * @return a HEAD request builder object
     */
    public HeadBuilder head(final URI url) {
        return new HeadBuilder(url, this);
    }

    /**
     * Make a OPTIONS request to output information about the supported HTTP methods, etc.
     *
     * @param url the URL of the resource to make the OPTIONS request on.
     * @return a OPTIONS request builder object
     */
    public OptionsBuilder options(final URI url) {
        return new OptionsBuilder(url, this);
    }

    @Override
    public void close() throws IOException {
        this.httpclient.close();
    }

    /**
     * Execute a HTTP request
     *
     * @param url URI the request is made to
     * @param request the request
     * @return the repository response
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public FcrepoResponse executeRequest(final URI url, final HttpRequestBase request)
            throws FcrepoOperationFailedException {
        LOGGER.debug("Fcrepo {} request to resource {}", request.getMethod(), url);
        final CloseableHttpResponse response = executeRequest(request);

        return fcrepoGenericResponse(url, response, throwExceptionOnFailure);
    }

    /**
     * Execute the HTTP request
     */
    private CloseableHttpResponse executeRequest(final HttpRequestBase request)
            throws FcrepoOperationFailedException {
        try {
            return httpclient.execute(request);
        } catch (final IOException ex) {
            LOGGER.debug("HTTP Operation failed: ", ex);
            throw new FcrepoOperationFailedException(request.getURI(), -1, ex.getMessage());
        }
    }

    /**
     * Handle the general case with responses.
     */
    private FcrepoResponse fcrepoGenericResponse(final URI url, final CloseableHttpResponse response,
            final Boolean throwExceptionOnFailure) throws FcrepoOperationFailedException {
        final int status = response.getStatusLine().getStatusCode();
        final Map<String, List<String>> headers = getHeaders(response);

        if ((status >= HttpStatus.SC_OK && status < HttpStatus.SC_BAD_REQUEST) || !throwExceptionOnFailure) {
            return new FcrepoResponse(url, status, headers, getEntityContent(response));
        } else {
            free(response);
            throw new FcrepoOperationFailedException(url, status,
                    response.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * Frees resources associated with the HTTP response. Specifically, closing the {@code response} frees the
     * connection of the {@link org.apache.http.conn.HttpClientConnectionManager} underlying this {@link #httpclient}.
     *
     * @param response the response object to close
     */
    private void free(final CloseableHttpResponse response) {
        // Free resources associated with the response.
        try {
            response.close();
        } catch (final IOException e) {
            LOGGER.warn("Unable to close HTTP response.", e);
        }
    }

    /**
     * Extract the response body as an input stream
     */
    private static InputStream getEntityContent(final HttpResponse response) {
        try {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            } else {
                return entity.getContent();
            }
        } catch (final IOException ex) {
            LOGGER.debug("Unable to extract HttpEntity response into an InputStream: ", ex);
            return null;
        }
    }

    /**
     * Retrieve all header values
     *
     * @param response response from request
     * @return Map of all values for all response headers
     */
    private static Map<String, List<String>> getHeaders(final HttpResponse response) {
        final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (final Header header : response.getAllHeaders()) {
            final List<String> values;
            if (headers.containsKey(header.getName())) {
                values = headers.get(header.getName());
            } else {
                values = new ArrayList<>();
                headers.put(header.getName(), values);
            }
            values.add(header.getValue());
        }
        return headers;
    }

    /**
     * Builds an FcrepoClient
     *
     * @author bbpennel
     */
    public static class FcrepoClientBuilder {

        private String authUser;

        private String authPassword;

        private String authHost;

        private FcrepoResponse.TransactionURI transactionURI;

        private boolean throwExceptionOnFailure;

        /**
         * Add basic authentication credentials to this client
         *
         * @param username username for authentication
         * @param password password for authentication
         * @return the client builder
         */
        public FcrepoClientBuilder credentials(final String username, final String password) {
            this.authUser = username;
            this.authPassword = password;
            return this;
        }

        /**
         * Add an authentication scope to this client
         *
         * @param authHost authentication scope value
         * @return this builder
         */
        public FcrepoClientBuilder authScope(final String authHost) {
            this.authHost = authHost;

            return this;
        }

        /**
         * Client should throw exceptions when failures occur
         *
         * @return this builder
         */
        public FcrepoClientBuilder throwExceptionOnFailure() {
            this.throwExceptionOnFailure = true;
            return this;
        }

        /**
         * The transaction uri for the client to add to requests
         *
         * @param transactionURI the transaction uri
         * @return this builder
         */
        public FcrepoClientBuilder transactionURI(final FcrepoResponse.TransactionURI transactionURI) {
            this.transactionURI = transactionURI;
            return this;
        }

        /**
         * Get the client
         *
         * @return the client constructed by this builder
         */
        public FcrepoClient build() {
            final FcrepoHttpClientBuilder httpClient = new FcrepoHttpClientBuilder(authUser, authPassword, authHost);
            if (transactionURI == null) {
                return new FcrepoClient(httpClient, throwExceptionOnFailure);
            } else {
                return new TransactionalFcrepoClient(transactionURI, httpClient, throwExceptionOnFailure);
            }
        }
    }
}
