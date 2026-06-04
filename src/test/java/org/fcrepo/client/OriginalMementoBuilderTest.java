/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link OriginalMementoBuilder}.
 *
 * @author surfrdan
 */
@RunWith(MockitoJUnitRunner.class)
public class OriginalMementoBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

    private OriginalMementoBuilder testBuilder;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);
        uri = create(baseUrl);
        testBuilder = new OriginalMementoBuilder(uri, client);
    }

    @Test
    public void testCreateIssuesPost() throws Exception {
        testBuilder.perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals("POST", requestCaptor.getValue().getMethod());
    }

    @Test
    public void testAddHeader() throws Exception {
        testBuilder.addHeader("my-header", "head-val").perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals("head-val", requestCaptor.getValue().getFirstHeader("my-header").getValue());
    }

    @Test
    public void testAddLinkHeader() throws Exception {
        final FcrepoLink link = FcrepoLink.fromUri("http://example.com/link").type("foo").build();
        testBuilder.addLinkHeader(link).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals(link.toString(), requestCaptor.getValue().getFirstHeader(LINK).getValue());
    }
}
