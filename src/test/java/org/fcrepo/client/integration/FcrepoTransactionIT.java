/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client.integration;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;


import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FedoraHeaderConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests against the Fedora Transaction API
 *
 * @author mikejritter
 */
public class FcrepoTransactionIT extends AbstractResourceIT {

    private static FcrepoClient client;

    @BeforeClass
    public static void beforeClass() {
        client = FcrepoClient.client()
                             .credentials("fedoraAdmin", "fedoraAdmin")
                             .authScope("localhost")
                             .build();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        client.close();
    }

    @Test
    public void testTransactionCommit() throws Exception {
        final String location;

        try (final var response = client.transaction(new URI(SERVER_ADDRESS)).start().perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());
            location = response.getTransactionUri();
        }

        try (final var response = client.transaction(new URI(location)).commit().perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testTransactionKeepAlive() throws Exception {
        // todo: initial transaction currently returns Expires rather than Atomic-Expires, should handle beep beep
        // final String expiry;
        final String location;

        try (final var response = client.transaction(new URI(SERVER_ADDRESS)).start().perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());

            location = response.getTransactionUri();
            // expiry = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
        }

        try (final var response = client.transaction(new URI(location)).keepAlive().perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

            final var expiryFromStatus = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
            assertNotNull(expiryFromStatus);
            // assertBefore(expiry, expiryFromStatus);
        }
    }

    @Test
    public void testTransactionStatus() throws Exception {
        // todo: initial transaction currently returns Expires rather than Atomic-Expires, should handle beep beep
        // final String expiry;
        final String location;

        try (final var response = client.transaction(new URI(SERVER_ADDRESS)).start().perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());

            location = response.getTransactionUri();
            // expiry = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
        }

        try (final var response = client.transaction(new URI(location)).status().perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

            final var expiryFromStatus = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
            assertNotNull(expiryFromStatus);
            // assertEquals(expiry, expiryFromStatus);
        }
    }

    @Test
    public void testTransactionRollback() throws Exception {
        final String location;

        try (final var response = client.transaction(new URI(SERVER_ADDRESS)).start().perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());
            location = response.getTransactionUri();
        }

        try (final var response = client.transaction(new URI(location)).rollback().perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

}
