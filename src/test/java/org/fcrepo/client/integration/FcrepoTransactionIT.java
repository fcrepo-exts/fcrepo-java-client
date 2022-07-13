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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;


import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;
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
        final FcrepoResponse.TransactionURI location;

        try (final var response = client.transaction().start(new URI(SERVER_ADDRESS)).perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());
            location = response.getTransactionUri().orElseThrow(() -> new IllegalStateException("No tx found"));
        }

        try (final var response = client.transaction().commit(location).perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testTransactionKeepAlive() throws Exception {
        final String expiry;
        final FcrepoResponse.TransactionURI location;
        final var formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        try (final var response = client.transaction().start(new URI(SERVER_ADDRESS)).perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());

            location = response.getTransactionUri().orElseThrow(() -> new IllegalStateException("No tx found"));
            // the initial transaction currently returns Expires rather than Atomic-Expires
            expiry = response.getHeaderValue("Expires");
        }

        try (final var response = client.transaction().keepAlive(location).perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

            final var expiryFromStatus = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
            assertNotNull(expiryFromStatus);

            final var initialExpiration = Instant.from(formatter.parse(expiry));
            final var updatedExpiration = Instant.from(formatter.parse(expiryFromStatus));
            assertTrue(initialExpiration.isBefore(updatedExpiration));
        }
    }

    @Test
    public void testTransactionStatus() throws Exception {
        // the initial transaction currently returns Expires rather than Atomic-Expires, update if changed
        final String expiry;
        final FcrepoResponse.TransactionURI location;

        try (final var response = client.transaction().start(new URI(SERVER_ADDRESS)).perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());

            location = response.getTransactionUri().orElseThrow(() -> new IllegalStateException("No tx found"));
            expiry = response.getHeaderValue("Expires");
        }

        try (final var response = client.transaction().status(location).perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

            final var expiryFromStatus = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
            assertNotNull(expiryFromStatus);
            assertEquals(expiry, expiryFromStatus);
        }
    }

    @Test
    public void testTransactionRollback() throws Exception {
        final FcrepoResponse.TransactionURI location;

        try (final var response = client.transaction().start(new URI(SERVER_ADDRESS)).perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());
            location = response.getTransactionUri().orElseThrow(() -> new IllegalStateException("No tx found"));
        }

        try (final var response = client.transaction().rollback(location).perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

}
