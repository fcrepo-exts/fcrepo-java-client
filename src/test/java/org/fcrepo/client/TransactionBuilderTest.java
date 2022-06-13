/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

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
    public void testStartValidUris() {
        final var root = SERVER_ENDPOINT + "/rest";
        final var rootTrailingSlash = SERVER_ENDPOINT + "/rest/";
        final var txUri = SERVER_ENDPOINT + "/rest/fcr:tx";
        final var txUriTrailingSlash = SERVER_ENDPOINT + "/rest/fcr:tx/";

        final var builder1 = new TransactionBuilder(URI.create(root), client);
        builder1.start();

        final var builder2 = new TransactionBuilder(URI.create(rootTrailingSlash), client);
        builder2.start();

        final var builder3 = new TransactionBuilder(URI.create(txUri), client);
        builder3.start();

        final var builder4 = new TransactionBuilder(URI.create(txUriTrailingSlash), client);
        builder4.start();
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