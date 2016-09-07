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

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.IF_UNMODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author bbpennel
 */
@RunWith(MockitoJUnitRunner.class)
public class PutBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    private PutBuilder testBuilder;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);

        uri = create(baseUrl);
        testBuilder = new PutBuilder(uri, client);
    }

    @Test
    public void testPutNoBody() throws Exception {
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
                .digest("checksum")
                .perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("plain/text", request.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals("sha1=checksum", request.getFirstHeader(DIGEST).getValue());
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testPostClientError() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenThrow(new FcrepoOperationFailedException(uri, 415, "status"));

        testBuilder.perform();
    }

    @Test
    public void testWithModificationHeaders() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        final String etag = "123456";
        final String lastModified = "Mon, 19 May 2014 19:44:59 GMT";
        testBuilder.body(bodyStream, "plain/text")
                .ifMatch(etag)
                .ifUnmodifiedSince(lastModified)
                .perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("plain/text", request.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals(etag, request.getFirstHeader(IF_MATCH).getValue());
        assertEquals(lastModified, request.getFirstHeader(IF_UNMODIFIED_SINCE).getValue());
    }

    @Test
    public void testPreferLenient() throws Exception {
        testBuilder.preferLenient().perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals("handling=lenient; received=\"minimal\"", request.getFirstHeader(PREFER).getValue());
    }
}
