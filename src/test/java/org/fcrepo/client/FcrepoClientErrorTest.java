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

import static org.fcrepo.client.TestUtils.baseUrl;
import static org.fcrepo.client.TestUtils.rdfXml;
import static org.fcrepo.client.TestUtils.setField;
import static org.fcrepo.client.TestUtils.sparqlUpdate;
import static org.fcrepo.client.TestUtils.RDF_XML;
import static org.fcrepo.client.TestUtils.SPARQL_UPDATE;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static java.net.URI.create;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;

/**
 * @author acoburn
 */
@RunWith(MockitoJUnitRunner.class)
public class FcrepoClientErrorTest {

    private FcrepoClient testClient;

    @Mock
    private CloseableHttpClient mockHttpclient;

    @Mock
    private StatusLine mockStatus;

    @Mock
    private CloseableHttpResponse mockResponse;

    @Before
    public void setUp() throws IOException {
        testClient = new FcrepoClient(null, null, null, false);
        setField(testClient, "httpclient", mockHttpclient);
    }

    @Test
    public void testGet() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());

        entity.setContentType(RDF_XML);
        doSetupMockRequest(RDF_XML, entity, status);

        final FcrepoResponse response = testClient.get(uri, RDF_XML, null);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody()), rdfXml);
    }

    @Test
    public void testGetError() throws Exception {
        final int status = 400;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());

        entity.setContentType(RDF_XML);
        doSetupMockRequest(RDF_XML, entity, status);

        final FcrepoResponse response = testClient.get(uri, RDF_XML, "return=representation;");

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody()), rdfXml);
    }

    @Test
    public void testHead() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);

        doSetupMockRequest(TEXT_TURTLE, null, status);

        final FcrepoResponse response = testClient.head(uri);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), TEXT_TURTLE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testHeadError() throws IOException, FcrepoOperationFailedException {
        final int status = 400;
        final URI uri = create(baseUrl);

        doSetupMockRequest(TEXT_TURTLE, null, status);

        final FcrepoResponse response = testClient.head(uri);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), TEXT_TURTLE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPut() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(rdfXml.getBytes());

        doSetupMockRequest(RDF_XML, null, status);

        final FcrepoResponse response = testClient.put(uri, body, RDF_XML);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPutError() throws IOException, FcrepoOperationFailedException {
        final int status = 500;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(rdfXml.getBytes());

        doSetupMockRequest(RDF_XML, null, status);

        final FcrepoResponse response = testClient.put(uri, body, RDF_XML);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testDelete() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);

        doSetupMockRequest(null, null, status);

        final FcrepoResponse response = testClient.delete(uri);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testDeleteError() throws IOException, FcrepoOperationFailedException {
        final int status = 404;
        final URI uri = create(baseUrl);

        doSetupMockRequest(null, null, status);

        final FcrepoResponse response = testClient.delete(uri);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPatch() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.patch(uri, body);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPatchError() throws IOException, FcrepoOperationFailedException {
        final int status = 401;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.patch(uri, body);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPost() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.post(uri, body, SPARQL_UPDATE);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPostError() throws IOException, FcrepoOperationFailedException {
        final int status = 404;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.post(uri, body, SPARQL_UPDATE);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testHeaders() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);
        final Header contentTypeHeader = new BasicHeader("Content-Type", TEXT_TURTLE);
        final Header linkHeader = new BasicHeader("Link", "<" + baseUrl + "/bar>; rel=\"describedby\"");
        final Header linkFooHeader = new BasicHeader("Link" ,"<" + baseUrl + "/bar>; rel=\"foo\"");
        final Header[] linkHeaders = new Header[]{ linkHeader, linkFooHeader };

        when(mockHttpclient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getFirstHeader("Location")).thenReturn(null);
        when(mockResponse.getFirstHeader("Content-Type")).thenReturn(contentTypeHeader);
        when(mockResponse.getHeaders("Link")).thenReturn(linkHeaders);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(status);

        final FcrepoResponse response = testClient.head(uri);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), TEXT_TURTLE);
        assertEquals(response.getLocation(), create(baseUrl + "/bar"));
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testHeadersWithoutContentType() throws IOException, FcrepoOperationFailedException {
        final int status = 100;
        final URI uri = create(baseUrl);
        final Header linkHeader = new BasicHeader("Link", "<" + baseUrl + "/bar>; rel=\"describedby\"");
        final Header linkFooHeader = new BasicHeader("Link" ,"<" + baseUrl + "/bar>; rel=\"foo\"");
        final Header[] linkHeaders = new Header[]{ linkHeader, linkFooHeader };

        when(mockHttpclient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getFirstHeader("Location")).thenReturn(null);
        when(mockResponse.getFirstHeader("Content-Type")).thenReturn(null);
        when(mockResponse.getHeaders("Link")).thenReturn(linkHeaders);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(status);

        final FcrepoResponse response = testClient.head(uri);

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), create(baseUrl + "/bar"));
        assertEquals(response.getBody(), null);
    }

    private void doSetupMockRequest(final String contentType, final ByteArrayEntity entity, final int status)
            throws IOException {

        final Header contentTypeHeader = new BasicHeader("Content-Type", contentType);
        final Header[] linkHeaders = new Header[]{};
        final Header[] responseHeaders = new Header[] { contentTypeHeader };

        when(mockHttpclient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getFirstHeader("Location")).thenReturn(null);
        when(mockResponse.getFirstHeader("Content-Type")).thenReturn(contentTypeHeader);
        when(mockResponse.getHeaders("Link")).thenReturn(linkHeaders);
        when(mockResponse.getEntity()).thenReturn(entity);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(status);
        when(mockResponse.getAllHeaders()).thenReturn(responseHeaders);
    }
}
