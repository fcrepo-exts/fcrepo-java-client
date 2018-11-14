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
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.SLUG;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author bbpennel
 */
@RunWith(MockitoJUnitRunner.class)
public class PostBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    private PostBuilder testBuilder;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);

        uri = create(baseUrl);
        testBuilder = new PostBuilder(uri, client);
    }

    @Test
    public void testPostNoBody() throws Exception {
        testBuilder.perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertNull("Request body should not be set", request.getEntity());
        assertEquals(0, request.getAllHeaders().length);
    }

    @Test
    public void testWithBody() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream, "plain/text")
                .digestSha1("checksum")
                .filename("file.txt")
                .slug("slug_value")
                .perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("plain/text", request.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals("sha1=checksum", request.getFirstHeader(DIGEST).getValue());
        assertEquals("slug_value", request.getFirstHeader(SLUG).getValue());
        assertEquals("attachment; filename=\"file.txt\"", request.getFirstHeader(CONTENT_DISPOSITION).getValue());
    }

    @Test
    public void testAttachment() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);
        testBuilder.body(bodyStream, "plain/text").filename(null).perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());
        assertEquals("attachment", request.getFirstHeader(CONTENT_DISPOSITION).getValue());
    }

    @Test
    public void testWithBodyMultipleChecksums() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream, "plain/text")
                .digestSha1("checksum")
                .digestSha256("checksum256")
                .perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("plain/text", request.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals("sha1=checksum, sha256=checksum256", request.getFirstHeader(DIGEST).getValue());
    }

    @Test
    public void testBodyNoType() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream).perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());
        assertEquals("application/octet-stream", request.getFirstHeader(CONTENT_TYPE).getValue());
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testPostClientError() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenThrow(new FcrepoOperationFailedException(uri, 415, "status"));

        testBuilder.perform();
    }
}
