package org.fcrepo.client;

import org.apache.http.HttpStatus;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Expectations for the Mock Http Server
 *
 * @author esm
 */
public class MockHttpExpectations implements ExpectationInitializer {

    @Override
    public void initializeExpectations(final MockServerClient mockServerClient) {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/foo")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.SC_NOT_FOUND)
        );

        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/exists")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.SC_CREATED)
        );

    }

}
