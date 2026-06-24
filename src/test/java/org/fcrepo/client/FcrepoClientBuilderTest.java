/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FcrepoClient.FcrepoClientBuilder}.
 *
 * @author surfrdan
 */
public class FcrepoClientBuilderTest {

    @Test
    public void testBuildDefault() throws Exception {
        try (final FcrepoClient client = FcrepoClient.client().build()) {
            assertNotNull(client);
        }
    }

    @Test
    public void testBuildWithCredentials() throws Exception {
        try (final FcrepoClient client = FcrepoClient.client()
                .credentials("user", "password")
                .authScope("localhost")
                .throwExceptionOnFailure()
                .build()) {
            assertNotNull(client);
        }
    }
}
