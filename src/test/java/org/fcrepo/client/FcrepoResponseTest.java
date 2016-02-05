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
package org.fcrepo.client;

import static org.junit.Assert.assertEquals;
import static java.net.URI.create;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.commons.io.IOUtils;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author ajs6f
 */
@RunWith(MockitoJUnitRunner.class)
public class FcrepoResponseTest {

    @Test
    public void testResponse() throws IOException {
        final URI uri = create("http://localhost/path/a/b");
        final int status = 200;
        final String contentType = "text/plain";
        final URI location = create("http://localhost/path/a/b/c");
        final String body = "Text response";
        final InputStream bodyStream = new ByteArrayInputStream(body.getBytes(UTF_8));
        final FcrepoResponse response = new FcrepoResponse(uri, status, contentType, location, bodyStream);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), contentType);
        assertEquals(response.getLocation(), location);
        assertEquals(IOUtils.toString(response.getBody(), UTF_8), body);

        response.setUrl(create("http://example.org/path/a/b"));
        assertEquals(response.getUrl(), create("http://example.org/path/a/b"));

        response.setStatusCode(301);
        assertEquals(response.getStatusCode(), 301);

        response.setContentType("application/n-triples");
        assertEquals(response.getContentType(), "application/n-triples");

        response.setLocation(create("http://example.org/path/a/b/c"));
        assertEquals(response.getLocation(), create("http://example.org/path/a/b/c"));

        response.setBody(new ByteArrayInputStream(
                    "<http://example.org/book/3> <dc:title> \"Title\" .".getBytes(UTF_8)));
        assertEquals(IOUtils.toString(response.getBody(), UTF_8),
                    "<http://example.org/book/3> <dc:title> \"Title\" .");
    }

    /**
     * Demonstrates that response objects are <em>not</em> {@code close()}ed by default, that the state of
     * {@link FcrepoResponse#closed} is set appropriately when {@link FcrepoResponse#close()} is invoked under normal
     * (i.e. no exception thrown during {@code close()}) conditions, and that {@link InputStream#close()} is not invoked
     * repeatedly after the {@code FcrepoResponse} has been {@code close()}ed.
     *
     * @throws IOException if something exceptional happens
     */
    @Test
    public void testClosableReleasesResources() throws IOException {
        final InputStream mockBody = mock(InputStream.class);
        final FcrepoResponse underTest = new FcrepoResponse(
                URI.create("http://localhost/foo"), 201, "text/plain", URI.create("http://localhost/bar"), mockBody);

        assertFalse("FcrepoResponse objects should not be closed until close() is invoked.", underTest.isClosed());

        underTest.close();
        assertTrue(underTest.isClosed());
        verify(mockBody, times(1)).close();

        underTest.close();
        assertTrue(underTest.isClosed());
        verify(mockBody, times(1)).close();
    }

    /**
     * Demonstrates that if an {@code IOException} is thrown by {@link FcrepoResponse#close()}, <em>and</em> an
     * exception is thrown inside of a client's {@code try} block, the {@code IOException} from the {@code close()}
     * method is properly appended as a suppressed exception.
     *
     * @throws IOException if something exceptional happens
     */
    @Test
    public void testClosableSuppressedExceptions() throws IOException {
        final InputStream mockBody = mock(InputStream.class);
        final IOException notSuppressed = new IOException("Not suppressed.");
        final IOException suppressed = new IOException("Suppressed");
        doThrow(suppressed).when(mockBody).close();

        try (FcrepoResponse underTest = new FcrepoResponse(URI.create("http://localhost/foo"), 201, "text/plain",
                URI.create("http://localhost/bar"), mockBody)) {
            assertFalse(underTest.isClosed());

            throw notSuppressed;

        } catch (Exception e) {
            assertSame(notSuppressed, e);
            assertTrue(e.getSuppressed() != null && e.getSuppressed().length == 1);
            assertSame(suppressed, e.getSuppressed()[0]);
        }

        verify(mockBody).close();
    }

    /**
     * Demonstrates a successful idiomatic usage with try-with-resources
     *
     * @throws FcrepoOperationFailedException if something exceptional happens
     */
    @Test
    public void testIdiomaticInvokation() throws FcrepoOperationFailedException {
        final String content = "Hello World!";
        final ByteArrayInputStream entityBody = new ByteArrayInputStream(content.getBytes());
        final FcrepoClient client = mock(FcrepoClient.class);

        when(client.get(any(URI.class), any(String.class), any(String.class))).thenReturn(
                new FcrepoResponse(null, 200, null, null, entityBody));

        try (FcrepoResponse res = client.get(URI.create("foo"), "", "")) {
            assertEquals(content, IOUtils.toString(res.getBody()));
        } catch (IOException e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * Demonstrates idiomatic exception handling with try-with-resources
     *
     * @throws Exception if something exceptional happens
     */
    @Test
    public void testIdiomaticInvokationThrowsException() throws Exception {
        final InputStream mockBody = mock(InputStream.class);
        final IOException ioe = new IOException("Mocked IOE");
        when(mockBody.read(any(byte[].class))).thenThrow(ioe);

        final FcrepoClient client = mock(FcrepoClient.class);
        when(client.get(any(URI.class), any(String.class), any(String.class))).thenReturn(
                new FcrepoResponse(null, 200, null, null, mockBody));

        try (FcrepoResponse res = client.get(URI.create("foo"), "", "")) {
            ByteStreams.copy(res.getBody(), NullOutputStream.NULL_OUTPUT_STREAM);
            fail("Expected an IOException to be thrown.");
        } catch (IOException e) {
            assertSame(ioe, e);
        }

        verify(mockBody).close();
    }

}
