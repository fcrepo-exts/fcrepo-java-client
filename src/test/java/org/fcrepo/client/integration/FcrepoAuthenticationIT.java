/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;
import org.junit.Test;

/**
 * @author mohideen
 */
public class FcrepoAuthenticationIT extends AbstractResourceIT {

    protected static FcrepoClient authClient;

    public FcrepoAuthenticationIT() throws Exception {
        super();

        client = FcrepoClient.client().build();
        authClient = FcrepoClient.client()
                .credentials("fedoraAdmin", "password")
                .authScope("localhost")
                .build();
    }

    @Test
    public void testAuthUserCanPut() throws Exception {

        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = authClient.put(new URI(serverAddress + "testobj1"))
                .body(body, TEXT_TURTLE)
                .perform();
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotPut() throws Exception {
        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = client.put(new URI(serverAddress + "testobj2"))
                .body(body, TEXT_TURTLE)
                .perform();
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                FORBIDDEN.getStatusCode(), status);
    }

    @Test
    public void testAuthUserCanPatch() throws Exception {
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        final FcrepoResponse response = authClient.patch(new URI(serverAddress + "testobj1"))
                .body(body)
                .perform();
        final int status = response.getStatusCode();
        assertEquals("Didn't get a successful PATCH response! Got content:\n",
                NO_CONTENT.getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotPatch() throws Exception {
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        final FcrepoResponse response = client.patch(new URI(serverAddress + "testobj1"))
                .body(body)
                .perform();
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                FORBIDDEN.getStatusCode(), status);
    }

    @Test
    public void testAuthUserCanPost() throws Exception {
        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = authClient.post(new URI(serverAddress))
                .body(body, TEXT_TURTLE)
                .perform();
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotPost() throws Exception {
        final InputStream body = new ByteArrayInputStream(rdfTtl.getBytes());
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(body, TEXT_TURTLE)
                .perform();
        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden! Got content:\n" + content,
                FORBIDDEN.getStatusCode(), status);
    }

    @Test
    public void testAuthUserCanGet()
            throws Exception {
        final FcrepoResponse response = authClient.get(new URI(serverAddress)).perform();
        final int status = response.getStatusCode();
        assertEquals("Authenticated user can not read root!", OK
                .getStatusCode(), status);
    }

    @Test
    public void testUnAuthUserCannotGet()
            throws Exception {
        final FcrepoResponse response = client.get(new URI(serverAddress)).perform();
        final int status = response.getStatusCode();
        assertEquals("Unauthenticated user should be forbidden!", FORBIDDEN
                .getStatusCode(), status);
    }
}
