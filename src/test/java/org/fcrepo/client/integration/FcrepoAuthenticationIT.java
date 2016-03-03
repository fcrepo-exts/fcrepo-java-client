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
package org.fcrepo.client.integration;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.fcrepo.client.TestUtils.rdfTtl;
import static org.fcrepo.client.TestUtils.sparqlUpdate;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mohideen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-test/test-container.xml")
public class FcrepoAuthenticationIT {

    private static Logger logger = getLogger(FcrepoAuthenticationIT.class);

    protected static final int SERVER_PORT = Integer.parseInt(System
            .getProperty("fcrepo.dynamic.test.port", "8080"));

    protected static final String HOSTNAME = "localhost";

    protected static final String serverAddress = "http://" + HOSTNAME + ":" +
            SERVER_PORT + "/rest/";

    protected final PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager();

    protected static FcrepoClient client;

    protected static FcrepoClient authClient;

    public FcrepoAuthenticationIT() throws Exception {
        connectionManager.setMaxTotal(Integer.MAX_VALUE);
        connectionManager.setDefaultMaxPerRoute(20);
        connectionManager.closeIdleConnections(3, TimeUnit.SECONDS);
        client = new FcrepoClient(null, null, null, false);
        authClient = new FcrepoClient("fedoraAdmin", "password", "localhost", false);
    }

    @Test
    public void testAuthUserCanPut() throws Exception {

        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = authClient.put(new URI(serverAddress + "testobj1"), body, TEXT_TURTLE);
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotPut() throws Exception {
        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = client.put(new URI(serverAddress + "testobj2"), body, TEXT_TURTLE);
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                FORBIDDEN.getStatusCode(), status);
    }

    @Test
    public void testAuthUserCanPatch() throws Exception {
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        final FcrepoResponse response = authClient.patch(new URI(serverAddress + "testobj1"), body);
        final int status = response.getStatusCode();
        assertEquals("Didn't get a successful PATCH response! Got content:\n",
                NO_CONTENT.getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotPatch() throws Exception {
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        final FcrepoResponse response = client.patch(new URI(serverAddress + "testobj1"), body);
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                FORBIDDEN.getStatusCode(), status);
    }

    @Test
    public void testAuthUserCanPost() throws Exception {
        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = authClient.post(new URI(serverAddress), body, TEXT_TURTLE);
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotPost() throws Exception {
        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = client.post(new URI(serverAddress), body, TEXT_TURTLE);
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                FORBIDDEN.getStatusCode(), status);
    }

    @Test
    public void testAuthUserCanGet()
            throws Exception {
        final FcrepoResponse response = authClient.get(new URI(serverAddress), null, null);
        final int status = response.getStatusCode();
        assertEquals("Authenticated user can not read root!", OK
                .getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotGet()
            throws Exception {
        final FcrepoResponse response = client.get(new URI(serverAddress), null, null);
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden!", FORBIDDEN
                .getStatusCode(), status);
    }
}
