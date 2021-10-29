/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.net.URI;

import org.apache.http.HttpStatus;
import org.mockserver.client.MockServerClient;

/**
 * Expectations for the Mock Http Server
 *
 * @author esm
 */
public final class MockHttpExpectations {

    /**
     * The TCP host the mock HTTP server listens to.
     */
    final static String host = "localhost";

    /**
     * The TCP port the mock HTTP server listens to.
     */
    static String port;

    final static class Uris {
        int statusCode;
        String suffix;
        String path;

        Uris(final int statusCode) {
            this.statusCode = statusCode;
            this.path = "/uri/" + statusCode;
        }

        Uris(final int statusCode, final String suffix) {
            this.statusCode = statusCode;
            this.suffix = suffix;
            this.path = "/uri/" + statusCode + suffix;
        }

        URI asUri() {
            return URI.create("http://" + host + ":" + port + path);
        }

        @Override
        public String toString() {
            return asUri().toString();
        }
    }

    public final static class SupportedUris {

        /**
         * A request URI that will return a 500.
         */
        final Uris uri500 = new Uris(500);

        /**
         * A request URI that will return a 201.
         */
        final Uris uri201 = new Uris(201);

        /**
         * A request URI that will return a 200.
         */
        final Uris uri200 = new Uris(200);

        /**
         * A request URI that will return a 200 with a text response body.
         */
        final public Uris uri200RespBody = new Uris(200, "RespBody");
    }

    /**
     *
     * @param mockServerClient the mock HTTP server to be configured
     * @param port the port the mock HTTP server is running on
     */
    public void initializeExpectations(final MockServerClient mockServerClient, final int port) {

        MockHttpExpectations.port = String.valueOf(port);

        mockServerClient.when(
                request()
                        .withPath("/uri/500")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        );

        mockServerClient.when(
                request()
                        .withPath("/uri/201")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.SC_CREATED)
        );


        mockServerClient.when(
                request()
                        .withPath("/uri/200")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.SC_OK)
        );

        mockServerClient.when(
                request()
                        .withPath("/uri/200RespBody")
        ).respond(
                response("Response body")
                        .withStatusCode(HttpStatus.SC_OK)
        );
    }

}
