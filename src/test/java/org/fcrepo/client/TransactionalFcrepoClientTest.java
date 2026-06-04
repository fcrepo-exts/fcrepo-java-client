/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.ATOMIC_ID;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link TransactionalFcrepoClient}.
 *
 * @author surfrdan
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionalFcrepoClientTest {

    @Mock
    private FcrepoHttpClientBuilder httpClientBuilder;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

    private URI txUri;

    private URI resourceUri;

    private TransactionalFcrepoClient txClient;

    @Before
    public void setUp() throws Exception {
        when(httpClientBuilder.build()).thenReturn(httpClient);
        when(httpClient.execute(any(HttpRequestBase.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getAllHeaders()).thenReturn(new Header[0]);

        txUri = create("http://localhost:8080/rest/tx:1234");
        resourceUri = create(baseUrl);
        txClient = new TransactionalFcrepoClient(txUri, httpClientBuilder, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTransactionUri() {
        new TransactionalFcrepoClient(null, httpClientBuilder, true);
    }

    @Test
    public void testGetTransactionUri() {
        assertEquals(txUri, txClient.getTransactionURI());
    }

    @Test
    public void testCommitPutsToTransactionUri() throws Exception {
        txClient.commit().perform();
        assertRequest("PUT", txUri);
    }

    @Test
    public void testStatusGetsTransactionUri() throws Exception {
        txClient.status().perform();
        assertRequest("GET", txUri);
    }

    @Test
    public void testKeepAlivePostsToTransactionUri() throws Exception {
        txClient.keepAlive().perform();
        assertRequest("POST", txUri);
    }

    @Test
    public void testRollbackDeletesTransactionUri() throws Exception {
        txClient.rollback().perform();
        assertRequest("DELETE", txUri);
    }

    @Test
    public void testGetAddsAtomicIdHeader() throws Exception {
        txClient.get(resourceUri).perform();
        assertRequest("GET", resourceUri);
    }

    @Test
    public void testHeadAddsAtomicIdHeader() throws Exception {
        txClient.head(resourceUri).perform();
        assertRequest("HEAD", resourceUri);
    }

    @Test
    public void testDeleteAddsAtomicIdHeader() throws Exception {
        txClient.delete(resourceUri).perform();
        assertRequest("DELETE", resourceUri);
    }

    @Test
    public void testOptionsAddsAtomicIdHeader() throws Exception {
        txClient.options(resourceUri).perform();
        assertRequest("OPTIONS", resourceUri);
    }

    @Test
    public void testPatchAddsAtomicIdHeader() throws Exception {
        txClient.patch(resourceUri).perform();
        assertRequest("PATCH", resourceUri);
    }

    @Test
    public void testPostAddsAtomicIdHeader() throws Exception {
        txClient.post(resourceUri).perform();
        assertRequest("POST", resourceUri);
    }

    @Test
    public void testPutAddsAtomicIdHeader() throws Exception {
        txClient.put(resourceUri).perform();
        assertRequest("PUT", resourceUri);
    }

    @Test
    public void testTransactionalClientFromResponse() {
        final FcrepoResponse response = org.mockito.Mockito.mock(FcrepoResponse.class);
        when(response.getTransactionUri()).thenReturn(txUri);
        final TransactionalFcrepoClient derived = txClient.transactionalClient(response);
        assertEquals(txUri, derived.getTransactionURI());
    }

    /**
     * Capture the request issued to the underlying http client and assert its method, target uri, and that the
     * transaction atomic-id header was added.
     */
    private void assertRequest(final String method, final URI target) throws Exception {
        verify(httpClient).execute(requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals(method, request.getMethod());
        assertEquals(target, request.getURI());
        assertEquals(txUri.toString(), request.getFirstHeader(ATOMIC_ID).getValue());
    }
}
