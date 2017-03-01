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
import static org.fcrepo.client.FedoraHeaderConstants.ACCEPT;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.IF_NONE_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;
import static org.fcrepo.client.FedoraHeaderConstants.RANGE;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.EntityTag;

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
public class GetBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    private GetBuilder testBuilder;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);

        uri = create(baseUrl);
        testBuilder = new GetBuilder(uri, client);
    }

    @Test
    public void testGet() throws Exception {
        testBuilder.perform();

        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpRequestBase request = getRequest();
        assertEquals(0, request.getAllHeaders().length);
    }

    @Test
    public void testPreferMinimal() throws Exception {
        testBuilder.preferMinimal().perform();

        final HttpRequestBase request = getRequest();
        assertEquals("return=minimal", request.getFirstHeader(PREFER).getValue());
    }

    @Test
    public void testPreferRepresentation() throws Exception {
        testBuilder.preferRepresentation().perform();

        final HttpRequestBase request = getRequest();
        assertEquals("return=representation", request.getFirstHeader(PREFER).getValue());
    }

    @Test
    public void testPreferInclude() throws Exception {
        final List<URI> includes = Arrays.asList(
                new URI("http://fedora.info/definitions/v4/repository#InboundReferences"),
                new URI("http://www.w3.org/ns/ldp#PreferMembership"));
        testBuilder.preferRepresentation(includes, null).perform();

        final HttpRequestBase request = getRequest();
        assertEquals("return=representation; include=\"" +
                "http://fedora.info/definitions/v4/repository#InboundReferences" +
                " http://www.w3.org/ns/ldp#PreferMembership\"",
                request.getFirstHeader(PREFER).getValue());
    }

    @Test
    public void testModificationHeaders() throws Exception {
        final String etag = "123456";
        final String lastModified = "Mon, 19 May 2014 19:44:59 GMT";
        testBuilder.ifNoneMatch(etag).ifModifiedSince(lastModified).perform();

        final HttpRequestBase request = getRequest();
        assertEquals(etag, EntityTag.valueOf(request.getFirstHeader(IF_NONE_MATCH).getValue()).getValue());
        assertEquals(lastModified, request.getFirstHeader(IF_MODIFIED_SINCE).getValue());
    }

    @Test
    public void testRange() throws Exception {
        testBuilder.range(5L, 100L).perform();

        final HttpRequestBase request = getRequest();
        assertEquals("bytes=5-100", request.getFirstHeader(RANGE).getValue());
    }

    @Test
    public void testStartRange() throws Exception {
        testBuilder.range(5L, null).perform();

        final HttpRequestBase request = getRequest();
        assertEquals("bytes=5-", request.getFirstHeader(RANGE).getValue());
    }

    @Test
    public void testEndRange() throws Exception {
        testBuilder.range(null, 100L).perform();

        final HttpRequestBase request = getRequest();
        assertEquals("bytes=-100", request.getFirstHeader(RANGE).getValue());
    }

    @Test
    public void testAccept() throws Exception {
        testBuilder.accept("text/turtle").perform();

        final HttpRequestBase request = getRequest();
        assertEquals("text/turtle", request.getFirstHeader(ACCEPT).getValue());
    }

    @Test
    public void testDisableRedirects() throws Exception {
        testBuilder.disableRedirects();
        assertFalse(testBuilder.request.getConfig().isRedirectsEnabled());
    }

    private HttpRequestBase getRequest() throws FcrepoOperationFailedException {
        final ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        return requestCaptor.getValue();
    }
}
