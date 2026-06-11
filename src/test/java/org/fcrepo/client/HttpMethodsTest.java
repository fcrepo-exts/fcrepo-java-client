/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Aaron Coburn
 */
@RunWith(JUnit4.class)
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
    public void testCreateRequestSetsUri() {
        final URI uri = create("http://localhost:8080/rest/foo");
        final HttpRequestBase request = HttpMethods.GET.createRequest(uri);
        assertEquals(uri, request.getURI());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testMoveRequest() {
        final URI uri = create("http://localhost:8080/rest/foo");
        final HttpRequestBase request = HttpMethods.MOVE.createRequest(uri);
        assertEquals("MOVE", request.getMethod());
        assertEquals(uri, request.getURI());
    }

    @Test
    public void testCopyRequest() {
        final URI uri = create("http://localhost:8080/rest/foo");
        final HttpRequestBase request = HttpMethods.COPY.createRequest(uri);
        assertEquals("COPY", request.getMethod());
        assertEquals(uri, request.getURI());
    }
}
