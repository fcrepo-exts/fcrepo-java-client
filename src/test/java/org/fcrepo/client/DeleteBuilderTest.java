/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.ATOMIC_ID;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link DeleteBuilder}.
 *
 * @author surfrdan
 */
@ExtendWith(MockitoExtension.class)
public class DeleteBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

    private DeleteBuilder testBuilder;

    private URI uri;

    @BeforeEach
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);
        uri = create(baseUrl);
        testBuilder = new DeleteBuilder(uri, client);
    }

    @Test
    public void testDeleteIssuesDelete() throws Exception {
        testBuilder.perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals("DELETE", requestCaptor.getValue().getMethod());
    }

    @Test
    public void testAddHeader() throws Exception {
        testBuilder.addHeader("my-header", "head-val").perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals("head-val", requestCaptor.getValue().getFirstHeader("my-header").getValue());
    }

    @Test
    public void testAddTransaction() throws Exception {
        final URI txUri = create("http://localhost:8080/rest/tx:1234");
        testBuilder.addTransaction(txUri).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals(txUri.toString(), requestCaptor.getValue().getFirstHeader(ATOMIC_ID).getValue());
    }
}
