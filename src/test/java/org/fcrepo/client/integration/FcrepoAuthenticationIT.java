/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client.integration;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.fcrepo.client.TestUtils.rdfTtl;
import static org.fcrepo.client.TestUtils.sparqlUpdate;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mohideen
 */
public class FcrepoAuthenticationIT extends AbstractResourceIT {

    private static FcrepoClient unauthClient;

    private static FcrepoClient authClient;

    private static FcrepoClient authClientNoHost;

    @BeforeClass
    public static void beforeClass() {

        unauthClient = FcrepoClient.client().credentials("testuser", "testpass")
                .authScope("localhost")
                .build();
        authClient = FcrepoClient.client()
                .credentials("fedoraAdmin", "fedoraAdmin")
                .authScope("localhost")
                .build();
        authClientNoHost = FcrepoClient.client()
                .credentials("fedoraAdmin", "fedoraAdmin")
                .build();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        unauthClient.close();
        authClient.close();
        authClientNoHost.close();
    }

    @Test
    public void testAuthUserCanPut() throws Exception {

        try (final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
                final FcrepoResponse response = authClient.put(new URI(SERVER_ADDRESS + "testobj1"))
                        .body(body, TEXT_TURTLE)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                    CREATED.getStatusCode(), status);
        }
    }

    @Test
    public void testAuthUserNoHostCanPut() throws Exception {

        try (final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
                final FcrepoResponse response = authClientNoHost.put(new URI(SERVER_ADDRESS + "testobj3"))
                        .body(body, TEXT_TURTLE)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                    CREATED.getStatusCode(), status);
        }
    }

    @Test
    public void testUnAuthUserCannotPut() throws Exception {
        try (final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
                final FcrepoResponse response = unauthClient.put(new URI(SERVER_ADDRESS + "testobj2"))
                        .body(body, TEXT_TURTLE)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                    FORBIDDEN.getStatusCode(), status);
        }
    }

    @Test
    public void testAuthUserCanPatch() throws Exception {

        try (final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
                final FcrepoResponse response = authClient.patch(new URI(SERVER_ADDRESS + "testobj1"))
                        .body(body)
                        .perform()) {
            final int status = response.getStatusCode();
            assertEquals("Didn't get a successful PATCH response! Got content:\n",
                    NO_CONTENT.getStatusCode(), status);
        }
    }

    @Test
    public void testAuthUserNoHostCanPatch() throws Exception {

        try (final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
                final FcrepoResponse response = authClientNoHost.patch(new URI(SERVER_ADDRESS + "testobj3"))
                        .body(body)
                        .perform()) {
            final int status = response.getStatusCode();
            assertEquals("Didn't get a successful PATCH response! Got content:\n",
                    NO_CONTENT.getStatusCode(), status);
        }
    }

    @Test
    public void testUnAuthUserCannotPatch() throws Exception {
        try (final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
                final FcrepoResponse response = unauthClient.patch(new URI(SERVER_ADDRESS + "testobj1"))
                        .body(body)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                    FORBIDDEN.getStatusCode(), status);
        }
    }

    @Test
    public void testAuthUserCanPost() throws Exception {
        try (final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
                final FcrepoResponse response = authClient.post(new URI(SERVER_ADDRESS))
                        .body(body, TEXT_TURTLE)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                    CREATED.getStatusCode(), status);
        }
    }

    @Test
    public void testAuthUserNoHostCanPost() throws Exception {
        try (final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
                final FcrepoResponse response = authClientNoHost.post(new URI(SERVER_ADDRESS))
                        .body(body, TEXT_TURTLE)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                    CREATED.getStatusCode(), status);
        }
    }

    @Test
    public void testUnAuthUserCannotPost() throws Exception {
        try (final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
                final FcrepoResponse response = unauthClient.post(new URI(SERVER_ADDRESS))
                        .body(body, TEXT_TURTLE)
                        .perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                    FORBIDDEN.getStatusCode(), status);
        }
    }

    @Test
    public void testAuthUserCanGet()
            throws Exception {
        try (final FcrepoResponse response = authClient.get(new URI(SERVER_ADDRESS)).perform()) {
            final int status = response.getStatusCode();
            assertEquals("Authenticated user can not read root!", OK
                    .getStatusCode(), status);
        }

    }

    @Test
    public void testAuthUserNoHostCanGet()
            throws Exception {
        try (final FcrepoResponse response = authClientNoHost.get(new URI(SERVER_ADDRESS)).perform()) {
            final int status = response.getStatusCode();
            assertEquals("Authenticated user can not read root!", OK
                    .getStatusCode(), status);
        }
    }

    @Ignore("Pending alignment with WebAC in FCREPO-2952")
    @Test
    public void testUnAuthUserCannotGet()
            throws Exception {
        try (final FcrepoResponse response = unauthClient.get(new URI(SERVER_ADDRESS)).perform()) {
            final int status = response.getStatusCode();
            assertEquals("Unauthenticated user should be forbidden!", FORBIDDEN
                    .getStatusCode(), status);
        }
    }
}
