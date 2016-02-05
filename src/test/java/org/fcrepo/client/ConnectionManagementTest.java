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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.fcrepo.client.MockHttpExpectations.host;
import static org.fcrepo.client.MockHttpExpectations.port;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.fcrepo.client.TestUtils.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Integration test used to demonstrate connection management issues with the FcrepoClient.
 *
 * @author esm
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionManagementTest {

    /**
     * Starts a mock HTTP server on a free port
     */
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    // Set by the above @Rule, initialized on @Before via MockHttpExpectations
    private MockServerClient mockServerClient;

    /**
     * URIs that our Mock HTTP server responds to.
     */
    private MockHttpExpectations.SupportedUris uris;

    /**
     * Verifies that the expected number of connections have been requested and then closed.
     *
     * @param connectionCount   the number of connections that have been requested and closed.
     * @param connectionManager the HttpClientConnectionManager
     * @return a Consumer that verifies the supplied HttpClientConnectionManager has opened and closed the expected
     * number of connections.
     */
    private static void verifyConnectionRequestedAndClosed(final int connectionCount,
                                                     final HttpClientConnectionManager connectionManager) {
        // A new connection was requested by the Http client ...
        verify(connectionManager, times(connectionCount)).requestConnection(any(HttpRoute.class), any());

        // Verify it was released.
        verify(connectionManager, times(connectionCount)).
                releaseConnection(any(HttpClientConnection.class), any(), anyLong(), any(TimeUnit.class));
    }

    /**
     * Verifies that the expected number of connections have been requested and <em>have not been</em> closed.
     *
     * @param connectionCount   the number of connections that have been requested.
     * @param connectionManager the HttpClientConnectionManager
     */
    private static void verifyConnectionRequestedButNotClosed(final int connectionCount,
                                                    final HttpClientConnectionManager connectionManager) {
        // A new connection was requested by the Http client ...
        verify(connectionManager, times(connectionCount)).requestConnection(any(HttpRoute.class), any());

        // Verify it was NOT released.
        verify(connectionManager, times(0)).
                releaseConnection(any(HttpClientConnection.class), any(), anyLong(), any(TimeUnit.class));
    }

    /**
     * FcrepoResponse handlers.
     */
    private static class FcrepoResponseHandler {

        /**
         * Closes the InputStream that constitutes the response body.
         */
        private static Consumer<FcrepoResponse> closeEntityBody = response ->
        {
            try {
                response.getBody().close();
            } catch (IOException e) {
                // ignore
            }
        };

        /**
         * Reads the InputStream that constitutes the response body.
         */
        private static Consumer<FcrepoResponse> readEntityBody = response -> {
            assertNotNull("Expected a non-null InputStream.", response.getBody());
            try {
                IOUtils.copy(response.getBody(), NullOutputStream.NULL_OUTPUT_STREAM);
            } catch (IOException e) {
                // ignore
            }
        };

    }

    /**
     * The Fedora Repository client.
     */
    private FcrepoClient client;

    /**
     * The Apache HttpClient under test.
     */
    private CloseableHttpClient underTest;

    /**
     * The {@link org.apache.http.conn.HttpClientConnectionManager} implementation that the {@link #underTest
     * HttpClient} is configured to used.
     */
    @Spy
    private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    @Before
    public void setUp() {

        // Required because we have a test that doesn't close connections, so we have to insure that the
        // connection manager doesn't block during that test.
        connectionManager.setDefaultMaxPerRoute(HttpMethods.values().length);

        // Set up the expectations on the Mock http server
        new MockHttpExpectations().initializeExpectations(this.mockServerClient, this.mockServerRule.getPort());

        // Uris that we connect to, and answered by the Mock http server
        uris = new MockHttpExpectations.SupportedUris();

        // A FcrepoClient configured to throw exceptions when an error is encountered.
        client = new FcrepoClient(null, null, host + ":" + port, true);

        // We're testing the behavior of a default HttpClient with a pooling connection manager.
        underTest = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

        // Put our testable HttpClient instance on the FcrepoClient
        setField(client, "httpclient", underTest);

    }

    @After
    public void tearDown() throws IOException {
        underTest.close();
    }

    /**
     * Demonstrates that HTTP connections are released when the FcrepoClient throws an exception.  Each method of the
     * FcrepoClient (get, put, post, etc.) is tested.
     */
    @Test
    public void connectionReleasedOnException() {
        // Hard-coded 6 b/c HttpMethods lists Options, which isn't supported by the FcrepoClient.
        final int expectedCount = 6;
        final AtomicInteger actualCount = new AtomicInteger(0);
        final MockHttpExpectations.Uris uri = uris.uri500;

        Stream.of(HttpMethods.values())
                // OPTIONS not supported by FcrepoClient
                .filter(method -> HttpMethods.OPTIONS != method)
                .forEach(method -> {
                    connect(client, uri, method, null);
                    actualCount.getAndIncrement();
                });

        assertEquals("Expected to make " + expectedCount + " connections; made " + actualCount.get(),
                expectedCount, actualCount.get());

        verifyConnectionRequestedAndClosed(actualCount.get(), connectionManager);
    }

    /**
     * Demonstrates that HTTP connections are released when the user of the FcrepoClient closes the HTTP entity body.
     * Each method of the FcrepoClient (get, put, post, etc.) is tested.
     */
    @Test
    public void connectionReleasedOnEntityBodyClose() {
        final int expectedCount = (int) Stream.of(HttpMethods.values()).filter(m -> m.entity).count();
        final AtomicInteger actualCount = new AtomicInteger(0);
        final MockHttpExpectations.Uris uri = uris.uri200;

        Stream.of(HttpMethods.values())
                .filter(method -> method.entity)
                .forEach(method -> {
                    connect(client, uri, method, FcrepoResponseHandler.closeEntityBody);
                    actualCount.getAndIncrement();
                });

        assertEquals("Expected to make " + expectedCount + " connections; made " + actualCount.get(),
                expectedCount, actualCount.get());
        verifyConnectionRequestedAndClosed(actualCount.get(), connectionManager);
    }

    /**
     * Demonstrates that are connections are released when the user of the FcrepoClient reads the HTTP entity body.
     */
    @Test
    public void connectionReleasedOnEntityBodyRead() {
        final int expectedCount = (int) Stream.of(HttpMethods.values()).filter(m -> m.entity).count();
        final AtomicInteger actualCount = new AtomicInteger(0);
        final MockHttpExpectations.Uris uri = uris.uri200;

        Stream.of(HttpMethods.values())
                .filter(method -> method.entity)
                .forEach(method -> {
                    connect(client, uri, method, FcrepoResponseHandler.readEntityBody);
                    actualCount.getAndIncrement();
                });

        assertEquals("Expected to make " + expectedCount + " connections; made " + actualCount.get(),
                expectedCount, actualCount.get());
        verifyConnectionRequestedAndClosed(actualCount.get(), connectionManager);
    }

    /**
     * Demonstrates that are connections are NOT released if the user of the FcrepoClient does not handle the response
     * body at all.
     */
    @Test
    public void connectionNotReleasedWhenEntityBodyIgnored() {
        final int expectedCount = (int) Stream.of(HttpMethods.values()).filter(m -> m.entity).count();
        final AtomicInteger actualCount = new AtomicInteger(0);
        final MockHttpExpectations.Uris uri = uris.uri200;

        Stream.of(HttpMethods.values())
                .filter(method -> method.entity)
                .forEach(method -> {
                    connect(client, uri, method, null);
                    actualCount.getAndIncrement();
                });

        assertEquals("Expected to make " + expectedCount + " connections; made " + actualCount.get(),
                expectedCount, actualCount.get());
        verifyConnectionRequestedButNotClosed(actualCount.get(), connectionManager);
    }

    /**
     * Uses the FcrepoClient to connect to supplied {@code uri} using the supplied {@code method}.
     * This method invokes the supplied {@code responseHandler} on the {@code FcrepoResponse}.
     *
     * @param client the FcrepoClient used to invoke the request
     * @param uri the request URI to connect to
     * @param method the HTTP method corresponding to the FcrepoClient method invoked
     * @param responseHandler invoked on the {@code FcrepoResponse}, may be {@code null}
     */
    private void connect(final FcrepoClient client, final MockHttpExpectations.Uris uri, final HttpMethods method,
                         final Consumer<FcrepoResponse> responseHandler) {

        final NullInputStream nullIn = new NullInputStream(1, true, false);
        FcrepoResponse response = null;

        try {

            switch (method) {

                case OPTIONS:
                    // not currently supported by the FcrepoClient
                    // intentionally throws an exception if the FcrepoClient implements OPTIONS in the future, to
                    // insure that it gets test coverage.
                    for (Method m : client.getClass().getDeclaredMethods()) {
                        if (m.getName().contains(method.name().toLowerCase())) {
                            fail("Untested method " + FcrepoClient.class.getName() + "#" + m.getName());
                        }
                    }

                    return;

                case DELETE:
                    response = client.delete(uri.asUri());
                    break;

                case GET:
                    response = client.get(uri.asUri(), null, TEXT_TURTLE);
                    break;

                case HEAD:
                    response = client.head(uri.asUri());
                    break;

                case PATCH:
                    response = client.patch(uri.asUri(), nullIn);
                    break;

                case POST:
                    response = client.post(uri.asUri(), nullIn, TEXT_TURTLE);
                    break;

                case PUT:
                    response = client.put(uri.asUri(), nullIn, TEXT_TURTLE);
                    break;

                default:
                    fail("Unknown HTTP method: " + method.name());
            }

            if (uri.statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                fail("Expected a FcrepoOperationFailedException to be thrown for HTTP method " + method.name());
            }
        } catch (FcrepoOperationFailedException e) {
            assertEquals(
                    "Expected request for " + uri.asUri() + " to return a " + uri.statusCode + ".  " +
                            "Was: " + e.getStatusCode(),
                    uri.statusCode, e.getStatusCode());
        } finally {
            if (responseHandler != null) {
                responseHandler.accept(response);
            }
        }
    }

}
