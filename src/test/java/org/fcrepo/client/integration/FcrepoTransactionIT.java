/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client.integration;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.fcrepo.client.FcrepoClient.TRANSACTION_ENDPOINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


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

    private static final String FEDORA_ADMIN = "fedoraAdmin";
    private static final DateTimeFormatter FORMATTER = RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC"));

    @BeforeClass
    public static void beforeClass() {
        client = FcrepoClient.client()
                             .credentials(FEDORA_ADMIN, FEDORA_ADMIN)
                             .authScope(HOSTNAME)
                             .build();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        client.close();
    }

    @Test
    public void testTransactionCommit() throws Exception {
        try (final var txClient = client.startTransactionClient(new URI(SERVER_ADDRESS + TRANSACTION_ENDPOINT))) {
            final var txURI = txClient.getTransactionURI();

            // create a container
            final var container = UUID.randomUUID().toString();
            try (final var response = txClient.put(new URI(SERVER_ADDRESS + container)).perform()) {
                assertEquals(CREATED.getStatusCode(), response.getStatusCode());
                assertNotNull(response.getTransactionUri());
                assertEquals(txURI.asString(), response.getTransactionUri().asString());
            }

            // commit tx
            try (final var response = txClient.transactionCommit().perform()) {
                assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
            }
        }
    }

    @Test
    public void testTransactionKeepAlive() throws Exception {
        final String expiry;
        final FcrepoResponse.TransactionURI location;

        try (final var txClient = client.startTransactionClient(new URI(SERVER_ADDRESS + TRANSACTION_ENDPOINT))) {
            location = txClient.getTransactionURI();
        assertNotNull(location);

            // the initial transaction currently returns Expires rather than Atomic-Expires
            try (var response = txClient.transactionStatus().perform()) {
                expiry = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
                assertNotNull(expiry);
            }

            // perform the keep-alive
            try (final var response = txClient.transactionKeepAlive().perform()) {
                assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

                final var expiryFromStatus = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
                assertNotNull(expiryFromStatus);

                final var initialExpiration = Instant.from(FORMATTER.parse(expiry));
                final var updatedExpiration = Instant.from(FORMATTER.parse(expiryFromStatus));
                assertTrue(initialExpiration.isBefore(updatedExpiration));
            }
        }
    }

    @Test
    public void testTransactionStatus() throws Exception {
        final FcrepoResponse.TransactionURI location;

        try (final var txClient = client.startTransactionClient(new URI(SERVER_ADDRESS + TRANSACTION_ENDPOINT))) {
            location = txClient.getTransactionURI();
            assertNotNull(location);

            try (final var response = txClient.transactionStatus().perform()) {
                assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

                final var expiryFromStatus = response.getHeaderValue(FedoraHeaderConstants.ATOMIC_EXPIRES);
                assertNotNull(expiryFromStatus);
            }
        }
    }

    @Test
    public void testTransactionRollback() throws Exception {
        final FcrepoResponse.TransactionURI location;

        try (final var txClient = client.startTransactionClient(new URI(SERVER_ADDRESS + TRANSACTION_ENDPOINT))) {
            location = txClient.getTransactionURI();
            assertNotNull(location);

            try (final var response = txClient.transactionRollback().perform()) {
                assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
            }
        }
    }

}
