/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests to check the TransactionBuilder constructor works as expected. Method testing done in TransactionBuilderIT.
 *
 * @author mikejritter
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionBuilderTest {

    private static final String SERVER_ENDPOINT = "http://localhost:8080";

    @Mock
    private FcrepoClient client;

    @Test
    public void testStartRootEndpoint() {
        final var root = SERVER_ENDPOINT + "/rest";
        final var builder = new TransactionBuilder(URI.create(root), client);
        builder.start();
    }

    @Test
    public void testStartRootTrailingSlash() {
        final var rootTrailingSlash = SERVER_ENDPOINT + "/rest/";
        final var builder = new TransactionBuilder(URI.create(rootTrailingSlash), client);
        builder.start();
    }

    @Test
    public void testStartTxUri() {
        final var txUri = SERVER_ENDPOINT + "/rest/fcr:tx";
        final var builder = new TransactionBuilder(URI.create(txUri), client);
        builder.start();
    }

    @Test
    public void testStartTxUriTrailingSlash() {
        final var txUriTrailingSlash = SERVER_ENDPOINT + "/rest/fcr:tx/";
        final var builder = new TransactionBuilder(URI.create(txUriTrailingSlash), client);
        builder.start();
    }

    @Test
    public void testCheckTxUriWithUUID() {
        final var txUriTrailingSlash = SERVER_ENDPOINT + "/rest/fcr:tx/" + UUID.randomUUID();
        final var builder = new TransactionBuilder(URI.create(txUriTrailingSlash), client);
        builder.keepAlive();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckInvalidAfterUUID() {
        final var txUriTrailingSlash = SERVER_ENDPOINT + "/rest/fcr:tx/" + UUID.randomUUID() + "/fcr:tx";
        final var builder = new TransactionBuilder(URI.create(txUriTrailingSlash), client);
        builder.keepAlive();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartNonRestEndpoint() {
        final var endpoint = SERVER_ENDPOINT + "/static";
        final var builder = new TransactionBuilder(URI.create(endpoint), client);
        builder.start();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartNonRestTxEndpoint() {
        final var endpoint = SERVER_ENDPOINT + "/static/fcr:tx";
        final var builder = new TransactionBuilder(URI.create(endpoint), client);
        builder.start();
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNonRestEndpoint() {
        final var endpoint = SERVER_ENDPOINT + "/static";
        final var builder = new TransactionBuilder(URI.create(endpoint), client);
        builder.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNonTxEndpoint() {
        final var endpoint = SERVER_ENDPOINT + "/rest/2478ec64-4a88-4a94-bad1-ba1251f48679/fcr:fixity";

        final var builder = new TransactionBuilder(URI.create(endpoint), client);
        builder.keepAlive();
    }

}