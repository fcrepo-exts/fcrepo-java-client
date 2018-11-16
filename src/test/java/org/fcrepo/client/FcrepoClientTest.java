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
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.LOCATION;
import static org.fcrepo.client.TestUtils.RDF_XML;
import static org.fcrepo.client.TestUtils.SPARQL_UPDATE;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.fcrepo.client.TestUtils.rdfXml;
import static org.fcrepo.client.TestUtils.setField;
import static org.fcrepo.client.TestUtils.sparqlUpdate;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author acoburn
 */
@RunWith(MockitoJUnitRunner.class)
public class FcrepoClientTest {

    private FcrepoClient testClient;

    @Mock
    private CloseableHttpClient mockHttpclient;

    @Mock
    private CloseableHttpResponse mockResponse;

    @Mock
    private StatusLine mockStatus;

    @Mock
    private HttpEntity mockEntity;

    @Before
    public void setUp() throws IOException {
        testClient = FcrepoClient.client().throwExceptionOnFailure().build();
        setField(testClient, "httpclient", mockHttpclient);
    }

    @Test
    public void testGet() throws IOException, FcrepoOperationFailedException {
        final int status = 200;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());
        entity.setContentType(RDF_XML);

        doSetupMockRequest(RDF_XML, entity, status);

        final FcrepoResponse response = testClient.get(uri)
                .accept(RDF_XML)
                .preferMinimal()
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody(), "UTF-8"), rdfXml);
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testGetError() throws Exception {
        final int status = 400;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());
        entity.setContentType(RDF_XML);

        doSetupMockRequest(RDF_XML, entity, status);
        testClient.get(uri)
                .accept(RDF_XML)
                .preferRepresentation()
                .perform();
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testGet100() throws Exception {
        final int status = 100;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());
        entity.setContentType(RDF_XML);

        doSetupMockRequest(RDF_XML, entity, status);
        testClient.get(uri)
                .accept(RDF_XML)
                .perform();
    }

    @Test
    public void testGet300() throws Exception {
        final int status = 300;
        final URI uri = create(baseUrl);
        final String redirect = baseUrl + "/bar";
        final Header linkHeader = new BasicHeader("Link", "<" + redirect + ">; rel=\"describedby\"");
        final Header contentType = new BasicHeader(CONTENT_TYPE, RDF_XML);
        final Header[] headers = new Header[] { contentType, linkHeader };
        final CloseableHttpResponse mockResponse = doSetupMockRequest(RDF_XML, null, status);

        when(mockResponse.getAllHeaders()).thenReturn(headers);

        final FcrepoResponse response = testClient.get(uri)
                .accept(RDF_XML)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), create(redirect));
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testGetNoAccept() throws Exception {
        final int status = 200;
        final URI uri = create(baseUrl);

        doSetupMockRequest(RDF_XML, null, status);

        final FcrepoResponse response = testClient.get(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testHead() throws IOException, FcrepoOperationFailedException {
        final int status = 200;
        final URI uri = create(baseUrl);

        doSetupMockRequest(TEXT_TURTLE, null, status);

        final FcrepoResponse response = testClient.head(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), TEXT_TURTLE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testHeadersCaseInsensitive() throws Exception {
        final int status = 200;
        final URI uri = create(baseUrl);

        doSetupMockRequest(TEXT_TURTLE, null, status);

        final FcrepoResponse response = testClient.head(uri).perform();

        // Verify that the case of header names returned by server doesn't impact retrieval
        assertEquals(response.getHeaderValue("content-type"), TEXT_TURTLE);
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testHeadError() throws IOException, FcrepoOperationFailedException {
        doSetupMockRequest(TEXT_TURTLE, null, 404);
        testClient.head(create(baseUrl)).perform();
    }

    @Test
    public void testPut() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(rdfXml.getBytes());

        doSetupMockRequest(RDF_XML, null, status);

        final FcrepoResponse response = testClient.put(uri)
                .body(body, RDF_XML)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPutNoBody() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);

        doSetupMockRequest(null, null, status);

        final FcrepoResponse response = testClient.put(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPutWithResponseBody() throws IOException, FcrepoOperationFailedException {
        final int status = 201;
        final URI uri = create(baseUrl);

        doSetupMockRequest(null, new ByteArrayEntity(uri.toString().getBytes()), status);

        final FcrepoResponse response = testClient.put(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody(), "UTF-8"), uri.toString());
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testPutError() throws IOException, FcrepoOperationFailedException {
        final int status = 500;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(rdfXml.getBytes());

        doSetupMockRequest(RDF_XML, null, status);
        testClient.put(uri)
                .body(body, RDF_XML)
                .perform();
    }

    @Test
    public void testDelete() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.delete(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testDeleteWithResponseBody() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final String responseText = "tombstone found";

        doSetupMockRequest(null, new ByteArrayEntity(responseText.getBytes()), status);

        final FcrepoResponse response = testClient.delete(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody(), "UTF-8"), responseText);
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testDeleteError() throws IOException, FcrepoOperationFailedException {
        final int status = 401;
        final URI uri = create(baseUrl);

        doSetupMockRequest(SPARQL_UPDATE, null, status);
        testClient.delete(uri).perform();
    }

    @Test
    public void testPatch() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.patch(uri)
                .body(body)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testPatchNoContent() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);

        doSetupMockRequest(SPARQL_UPDATE, null, status);
        testClient.patch(uri).perform();
    }

    @Test
    public void testPatchResponseBody() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final String responseText = "Sparql-update response";
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, new ByteArrayEntity(responseText.getBytes()), status);

        final FcrepoResponse response = testClient.patch(uri)
                .body(body)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(IOUtils.toString(response.getBody(), "UTF-8"), responseText);
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testPatchError() throws IOException, FcrepoOperationFailedException {
        final int status = 415;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);
        testClient.patch(uri)
                .body(body)
                .perform();
    }

    @Test
    public void testPost() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);

        final FcrepoResponse response = testClient.post(uri)
                .body(body, SPARQL_UPDATE)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    @Test
    public void testPostResponseBody() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final String responseText = baseUrl + "/bar";
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, new ByteArrayEntity(responseText.getBytes()), status);

        final FcrepoResponse response = testClient.post(uri)
                .body(body, SPARQL_UPDATE)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), SPARQL_UPDATE);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody(), "UTF-8"), responseText);
    }

    @Test
    public void testPostNoBody() throws IOException, FcrepoOperationFailedException {
        final int status = 204;
        final URI uri = create(baseUrl);
        final String responseText = baseUrl + "/bar";

        doSetupMockRequest(null, new ByteArrayEntity(responseText.getBytes()), status);

        final FcrepoResponse response = testClient.post(uri).perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), null);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody(), "UTF-8"), responseText);
    }

    @Test(expected = FcrepoOperationFailedException.class)
    public void testPostError() throws IOException, FcrepoOperationFailedException {
        final int status = 415;
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, null, status);
        testClient.post(uri)
                .body(body, SPARQL_UPDATE)
                .perform();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPostErrorNullUrl() throws Exception {
        final int status = 401;
        final String statusPhrase = "Unauthorized";
        final String response = "Response error";
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        final ByteArrayEntity responseBody = new ByteArrayEntity(response.getBytes());

        doSetupMockRequest(SPARQL_UPDATE, responseBody, status, statusPhrase);

        testClient.post(null)
                .body(body, SPARQL_UPDATE)
                .perform();
    }

    @Test
    public void testBadRequest() throws IOException, FcrepoOperationFailedException {
        final URI uri = create(baseUrl);
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        when(mockHttpclient.execute(any(HttpUriRequest.class))).thenThrow(new IOException("Expected error"));

        try {
            testClient.post(uri)
                    .body(body, SPARQL_UPDATE)
                    .perform();
        } catch (final FcrepoOperationFailedException ex) {
            assertEquals(ex.getUrl(), uri);
            assertEquals(ex.getStatusText(), "Expected error");
            assertEquals(ex.getStatusCode(), -1);
        }
    }

    @Test
    public void testBadResponseBody() throws IOException, FcrepoOperationFailedException {
        final int status = 200;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());
        entity.setContentType(RDF_XML);

        doSetupMockRequest(RDF_XML, entity, status);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenThrow(new IOException("Expected IO error"));

        final FcrepoResponse response = testClient.get(uri)
                .accept(RDF_XML)
                .preferMinimal()
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(response.getBody(), null);
    }

    private CloseableHttpResponse doSetupMockRequest(final String contentType, final ByteArrayEntity entity,
            final int status) throws IOException {
        return doSetupMockRequest(contentType, entity, status, null);
    }

    private CloseableHttpResponse doSetupMockRequest(final String contentType, final ByteArrayEntity entity,
            final int status, final String statusPhrase) throws IOException {
        final Header contentTypeHeader = new BasicHeader("Content-Type", contentType);
        final Header locationHeader = new BasicHeader(LOCATION, null);
        final Header[] responseHeaders = new Header[] { locationHeader, contentTypeHeader };

        when(mockHttpclient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getAllHeaders()).thenReturn(responseHeaders);
        when(mockResponse.getEntity()).thenReturn(entity);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(status);
        when(mockStatus.getReasonPhrase()).thenReturn(statusPhrase);

        return mockResponse;
    }
}
