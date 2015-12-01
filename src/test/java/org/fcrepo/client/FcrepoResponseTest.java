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

import java.net.URI;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

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

}
