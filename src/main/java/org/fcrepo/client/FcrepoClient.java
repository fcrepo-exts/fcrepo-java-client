/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.client;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class FcrepoClient {

    private CloseableHttpClient httpclient;

    private Boolean throwExceptionOnFailure = true;

    private static final Logger LOGGER = getLogger(FcrepoClient.class);

    /**
     * Build a FcrepoClient
     * 
     * @return
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

        final FcrepoHttpClientBuilder client = new FcrepoHttpClientBuilder(username, password, host);

        this.throwExceptionOnFailure = throwExceptionOnFailure;
        this.httpclient = client.build();
    }

    /**
     * Make a PUT request to create a resource with a specified path, or replace the triples associated with a
     * resource with the triples provided in the request body.
     * 
     * @param url the URL of the resource to which to PUT
     * @return a put request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public PutBuilder put(final URI url) throws FcrepoOperationFailedException {
        return new PutBuilder(url, this);
    }

    /**
     * Make a PATCH request to modify the triples associated with a resource with SPARQL-Update.
     * 
     * @param url the URL of the resource to which to PATCH
     * @return a patch request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public PatchBuilder patch(final URI url) throws FcrepoOperationFailedException {
        return new PatchBuilder(url, this);
    }

    /**
     * Make a POST request to create a new resource within an LDP container.
     * 
     * @param url the URL of the resource to which to POST
     * @return a post request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public PostBuilder post(final URI url) throws FcrepoOperationFailedException {
        return new PostBuilder(url, this);
    }

    /**
     * Make a DELETE request to delete a resource
     * 
     * @param url the URL of the resource to which to DELETE
     * @return a delete request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public DeleteBuilder delete(final URI url) throws FcrepoOperationFailedException {
        return new DeleteBuilder(url, this);
    }

    /**
     * Make a MOVE request to copy a resource (and its subtree) to a new location.
     * 
     * @param source url of the resource to copy
     * @param destination url of the location for the copy
     * @return a copy request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public CopyBuilder copy(final URI source, final URI destination) throws FcrepoOperationFailedException {
        return new CopyBuilder(source, destination, this);
    }

    /**
     * Make a COPY request to move a resource (and its subtree) to a new location.
     * 
     * @param source url of the resource to move
     * @param destination url of the new location for the resource
     * @return a move request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public MoveBuilder move(final URI source, final URI destination) throws FcrepoOperationFailedException {
        return new MoveBuilder(source, destination, this);
    }

    /**
     * Make a GET request to retrieve the content of a resource
     * 
     * @param url the URL of the resource to which to GET
     * @return a get request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public GetBuilder get(final URI url) throws FcrepoOperationFailedException {
        return new GetBuilder(url, this);
    }

    /**
     * Make a HEAD request to retrieve resource headers.
     * 
     * @param url the URL of the resource to make the HEAD request on.
     * @return a HEAD request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public HeadBuilder head(final URI url) throws FcrepoOperationFailedException {
        return new HeadBuilder(url, this);
    }

    /**
     * Make a OPTIONS request to output information about the supported HTTP methods, etc.
     * 
     * @param url the URL of the resource to make the OPTIONS request on.
     * @return a OPTIONS request builder object
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public OptionsBuilder options(final URI url) throws FcrepoOperationFailedException {
        return new OptionsBuilder(url, this);
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
        } catch (IOException ex) {
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
        } catch (IOException e) {
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
        } catch (IOException ex) {
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
        final Map<String, List<String>> headers = new HashMap<>();

        for (Header header : response.getAllHeaders()) {
            List<String> values;
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

        private boolean throwExceptionOnFailure;

        /**
         * Add basic authentication credentials to this client
         * 
         * @param username username
         * @param password
         * @return
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
         * Get the client
         * 
         * @return the client constructed by this builder
         */
        public FcrepoClient build() {
            return new FcrepoClient(authUser, authPassword, authHost, this.throwExceptionOnFailure);
        }
    }
}
