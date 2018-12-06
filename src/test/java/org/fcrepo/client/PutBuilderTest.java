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
import static org.fcrepo.client.ExternalContentHandling.PROXY;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.IF_STATE_TOKEN;
import static org.fcrepo.client.FedoraHeaderConstants.IF_UNMODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;
import static org.fcrepo.client.FedoraTypes.LDP_DIRECT_CONTAINER;
import static org.fcrepo.client.LinkHeaderConstants.EXTERNAL_CONTENT_HANDLING;
import static org.fcrepo.client.LinkHeaderConstants.EXTERNAL_CONTENT_REL;
import static org.fcrepo.client.LinkHeaderConstants.ACL_REL;
import static org.fcrepo.client.LinkHeaderConstants.TYPE_REL;
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
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author bbpennel
 */
@RunWith(MockitoJUnitRunner.class)
public class PutBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

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
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("plain/text", request.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals("sha=checksum", request.getFirstHeader(DIGEST).getValue());
        assertEquals("attachment; filename=\"file.txt\"", request.getFirstHeader(CONTENT_DISPOSITION).getValue());
    }

    @Test
    public void testExternalContent() throws Exception {
        final URI contentURI = URI.create("file:///path/to/file");
        testBuilder.externalContent(contentURI, "plain/text", PROXY)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();

        final FcrepoLink extLink = new FcrepoLink(request.getFirstHeader(LINK).getValue());
        assertEquals(EXTERNAL_CONTENT_REL, extLink.getRel());
        assertEquals(PROXY, extLink.getParams().get(EXTERNAL_CONTENT_HANDLING));
        assertEquals("plain/text", extLink.getType());
    }

    @Test
    public void testDisposition() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);
        testBuilder.body(bodyStream, "plain/text").filename(null).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());
        assertEquals("attachment", request.getFirstHeader(CONTENT_DISPOSITION).getValue());
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

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals("handling=lenient; received=\"minimal\"", request.getFirstHeader(PREFER).getValue());
    }

    @Test
    public void testAddInteractionModel() throws Exception {
        testBuilder.addInteractionModel(LDP_DIRECT_CONTAINER)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();

        final FcrepoLink interLink = new FcrepoLink(request.getFirstHeader(LINK).getValue());
        assertEquals(TYPE_REL, interLink.getRel());
        assertEquals(LDP_DIRECT_CONTAINER, interLink.getUri().toString());
    }

    @Test
    public void testLinkAcl() throws Exception {
        testBuilder.linkAcl("http://localhost/acl")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();

        final FcrepoLink aclLink = new FcrepoLink(request.getFirstHeader(LINK).getValue());
        assertEquals(ACL_REL, aclLink.getRel());
        assertEquals("http://localhost/acl", aclLink.getUri().toString());
    }

    @Test
    public void testStateToken() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);
        final String token = "state";

        testBuilder.body(bodyStream)
                .ifStateToken(token)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals(token, request.getFirstHeader(IF_STATE_TOKEN).getValue());
    }

    @Test
    public void testAddHeader() throws Exception {
        testBuilder.addHeader("my-header", "head-val").perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals("head-val", request.getFirstHeader("my-header").getValue());
    }

    @Test
    public void testAddLinkHeader() throws Exception {
        final FcrepoLink link = FcrepoLink.fromUri("http://example.com/link").type("foo").build();
        testBuilder.addLinkHeader(link).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals(link.toString(), request.getFirstHeader(LINK).getValue());
    }
}
