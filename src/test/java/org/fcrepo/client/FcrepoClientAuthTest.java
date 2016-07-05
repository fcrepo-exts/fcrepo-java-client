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
import static org.fcrepo.client.TestUtils.RDF_XML;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.fcrepo.client.TestUtils.rdfXml;
import static org.fcrepo.client.TestUtils.setField;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author acoburn
 */
@RunWith(MockitoJUnitRunner.class)
public class FcrepoClientAuthTest {

    private FcrepoClient testClient;

    @Mock
    private CloseableHttpClient mockHttpclient;

    @Mock
    private CloseableHttpResponse mockResponse;

    @Mock
    private StatusLine mockStatus;

    @Test
    public void testAuthNoHost() throws IOException, FcrepoOperationFailedException {
        final int status = 200;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());

        testClient = FcrepoClient.client()
                .credentials("user", "pass")
                .throwExceptionOnFailure()
                .build();
        setField(testClient, "httpclient", mockHttpclient);
        entity.setContentType(RDF_XML);
        doSetupMockRequest(RDF_XML, entity, status);

        final FcrepoResponse response = testClient.get(uri)
                .accept(RDF_XML)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody()), rdfXml);
    }

    @Test
    public void testAuthWithHost() throws IOException, FcrepoOperationFailedException {
        final int status = 200;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());

        testClient = FcrepoClient.client()
                .credentials("user", "pass")
                .authScope("localhost")
                .throwExceptionOnFailure()
                .build();
        setField(testClient, "httpclient", mockHttpclient);
        entity.setContentType(RDF_XML);
        doSetupMockRequest(RDF_XML, entity, status);

        final FcrepoResponse response = testClient.get(uri)
                .accept(RDF_XML)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody()), rdfXml);
    }

    @Test
    public void testAuthNoPassword() throws IOException, FcrepoOperationFailedException {
        final int status = 200;
        final URI uri = create(baseUrl);
        final ByteArrayEntity entity = new ByteArrayEntity(rdfXml.getBytes());

        testClient = FcrepoClient.client()
                .credentials("user", null)
                .throwExceptionOnFailure()
                .build();
        setField(testClient, "httpclient", mockHttpclient);
        entity.setContentType(RDF_XML);
        doSetupMockRequest(RDF_XML, entity, status);

        final FcrepoResponse response = testClient.get(uri)
                .accept(RDF_XML)
                .perform();

        assertEquals(response.getUrl(), uri);
        assertEquals(response.getStatusCode(), status);
        assertEquals(response.getContentType(), RDF_XML);
        assertEquals(response.getLocation(), null);
        assertEquals(IOUtils.toString(response.getBody()), rdfXml);
    }

    private void doSetupMockRequest(final String contentType, final ByteArrayEntity entity, final int status)
            throws IOException {
        final Header contentTypeHeader = new BasicHeader("Content-Type", contentType);
        final Header[] headers = new Header[] { contentTypeHeader };

        when(mockHttpclient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getAllHeaders()).thenReturn(headers);
        when(mockResponse.getEntity()).thenReturn(entity);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(status);
    }
}
