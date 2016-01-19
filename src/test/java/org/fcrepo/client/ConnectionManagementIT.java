package org.fcrepo.client;

import org.apache.http.HttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
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
public class ConnectionManagementIT {

    private static final String SERVER_PORT_PROPERTY = "fcrepo.httpclient.test.port";

    private static final String LDP_BASIC_CONTAINER_TRIPLE = "<> a http://www.w3.org/ns/ldp#BasicContainer";

    private String host = "localhost";

    private String port;

    private FcrepoClient client;

    private CloseableHttpClient underTest;

    @Spy
    private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    @Before
    public void setUp() throws Exception {
        port = System.getProperty(SERVER_PORT_PROPERTY);
        assertNotNull("Expected a port to be contained in the system property " + SERVER_PORT_PROPERTY, port);

        // A FcrepoClient configured to throw exceptions when an error is encountered.
        client = new FcrepoClient(null, null, "localhost:" + port, true);

        // We're testing the behavior of a default HttpClient with a pooling connection manager.
        underTest = HttpClientBuilder.create()
                           .setConnectionManager(connectionManager).build();

        // Put our testable HttpClient instance on the FcrepoClient
        setField(client, "httpclient", underTest);
    }

    /**
     * Demonstrates that connections are not released when the FcrepoClient throws an exception.  This is especially
     * problematic if clients are expected to manage connections, because the exception gives the client no access to
     * the response body.
     *
     * @throws Exception
     */
    @Test
    public void connectionNotReleasedOnException() throws Exception {

        // Connect to the mock endpoint.  A 404 is returned and the FcrepoClient throws an exception.
        try {
            client.post(fooUri(), new ByteArrayInputStream(LDP_BASIC_CONTAINER_TRIPLE.getBytes()), TEXT_TURTLE);
            fail("Expected a FcrepoOperationFailedException to be thrown.");
        } catch (FcrepoOperationFailedException e) {
            assertEquals("Expected request for " + fooUri().toString() + " to return a 404.  Was: " + e.getStatusCode(),
                    SC_NOT_FOUND, e.getStatusCode());
        }

        // A new connection was requested by the Http client ...
        verify(connectionManager).requestConnection(any(HttpRoute.class), any());

        // But it wasn't released.
        verify(connectionManager, times(0)).
                releaseConnection(any(HttpClientConnection.class), any(), anyLong(), any(TimeUnit.class));

    }

    /**
     * Demonstrates that connections are not released unless the user of FcrepoClient reads the response body
     *
     * @throws Exception
     */
    @Test
    public void connectionNotReleasedWithoutRead() throws Exception {

        // Connect to the mock endpoint.  A 201 is returned and the FcrepoClient creates a response
        final FcrepoResponse response = client.post(existsUri(),
                new ByteArrayInputStream(LDP_BASIC_CONTAINER_TRIPLE.getBytes()), TEXT_TURTLE);

        assertEquals("Expected request for " + existsUri().toString() + " to return a 201.  " +
                "Was: " + response.getStatusCode(), SC_CREATED, response.getStatusCode());

        // A new connection was requested by the Http client ...
        verify(connectionManager).requestConnection(any(HttpRoute.class), any());

        // But it wasn't released.
        verify(connectionManager, times(0)).
                releaseConnection(any(HttpClientConnection.class), any(), anyLong(), any(TimeUnit.class));

        // User reads the response body
        drainResponseBody(response.getBody());

        // Connection is released
        verify(connectionManager).
                releaseConnection(any(HttpClientConnection.class), any(), anyLong(), any(TimeUnit.class));

    }

    /**
     * Creates a URI that is able to connect to our mock HTTP endpoint and returns a 404.
     *
     * @return a valid URI that will return a 404
     */
    private URI fooUri() {
        return URI.create("http://" + host + ":" + port + "/foo");
    }

    /**
     * Creates a URI that is able to connect to our mock HTTP endpoint and returns a 201.
     *
     * @return a valid URI that will return a 201
     */
    private URI existsUri() {
        return URI.create("http://" + host + ":" + port + "/exists");
    }

    /**
     * Drains the response body input stream.
     *
     * @param responseBody the response body
     */
    private void drainResponseBody(final InputStream responseBody) {
        try {
            final byte[] buf = new byte[1024];
            int read = 0;
            do {
                read = responseBody.read(buf);

            } while (read > -1);

        } catch (IOException e) {
            // ignore
        }
    }

}
