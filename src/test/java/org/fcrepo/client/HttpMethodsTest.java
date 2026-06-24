/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import org.junit.jupiter.api.Test;

/**
 * @author Aaron Coburn
 */
public class HttpMethodsTest {

    @Test
    public void testMethods() {
        assertEquals(HttpMethods.DELETE.toString(), HttpDelete.METHOD_NAME);
        assertEquals(HttpMethods.GET.toString(), HttpGet.METHOD_NAME);
        assertEquals(HttpMethods.HEAD.toString(), HttpHead.METHOD_NAME);
        assertEquals(HttpMethods.OPTIONS.toString(), HttpOptions.METHOD_NAME);
        assertEquals(HttpMethods.PATCH.toString(), HttpPatch.METHOD_NAME);
        assertEquals(HttpMethods.POST.toString(), HttpPost.METHOD_NAME);
        assertEquals(HttpMethods.PUT.toString(), HttpPut.METHOD_NAME);
    }

    @Test
    public void testCreateRequestSetsUri() throws Exception {
        final URI uri = create("http://localhost:8080/rest/foo");
        final HttpUriRequestBase request = HttpMethods.GET.createRequest(uri);
        assertEquals(uri, request.getUri());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testMoveRequest() throws Exception {
        final URI uri = create("http://localhost:8080/rest/foo");
        final HttpUriRequestBase request = HttpMethods.MOVE.createRequest(uri);
        assertEquals("MOVE", request.getMethod());
        assertEquals(uri, request.getUri());
    }

    @Test
    public void testCopyRequest() throws Exception {
        final URI uri = create("http://localhost:8080/rest/foo");
        final HttpUriRequestBase request = HttpMethods.COPY.createRequest(uri);
        assertEquals("COPY", request.getMethod());
        assertEquals(uri, request.getUri());
    }
}
