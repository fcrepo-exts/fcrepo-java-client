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

import org.apache.http.HttpStatus;
import org.mockserver.client.server.MockServerClient;

import java.net.URI;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

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
        String path;

        Uris(final int statusCode) {
            this.statusCode = statusCode;
            this.path = "/uri/" + statusCode;
        }

        URI asUri() {
            return URI.create("http://" + host + ":" + port + path);
        }

        @Override
        public String toString() {
            return asUri().toString();
        }
    }

    final static class SupportedUris {

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

    }

}
