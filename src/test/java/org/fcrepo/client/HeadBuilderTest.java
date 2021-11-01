/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.ACCEPT_DATETIME;
import static org.fcrepo.client.FedoraHeaderConstants.CACHE_CONTROL;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.FedoraHeaderConstants.WANT_DIGEST;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author escowles
 */
@RunWith(MockitoJUnitRunner.class)
public class HeadBuilderTest {

    private final String HISTORIC_DATETIME =
            UTC_RFC_1123_FORMATTER.format(LocalDateTime.of(2000, 1, 1, 0, 0).atZone(ZoneOffset.UTC));

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    private HeadBuilder testBuilder;

    private URI uri;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);

        uri = create(baseUrl);
        testBuilder = new HeadBuilder(uri, client);
    }

    @Test
    public void testHead() throws Exception {
        testBuilder.perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
    }

    @Test
    public void testDisableRedirects() throws Exception {
        testBuilder.disableRedirects();
        assertFalse(testBuilder.request.getConfig().isRedirectsEnabled());
    }

    @Test
    public void testWantDigest() throws Exception {
        testBuilder.wantDigest("md5").perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals("md5", request.getFirstHeader(WANT_DIGEST).getValue());
    }

    @Test
    public void testNoCache() throws Exception {
        testBuilder.noCache().perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals("no-cache", request.getFirstHeader(CACHE_CONTROL).getValue());
    }

    @Test
    public void testAcceptDatetime() throws Exception {
        testBuilder.acceptDatetime(HISTORIC_DATETIME).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals(HISTORIC_DATETIME, request.getFirstHeader(ACCEPT_DATETIME).getValue());
    }

    @Test
    public void testAcceptDatetimeInstant() throws Exception {
        final Instant datetime = LocalDateTime.of(2000, 1, 1, 00, 00).atZone(ZoneOffset.UTC).toInstant();
        testBuilder.acceptDatetime(datetime).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals(HISTORIC_DATETIME, request.getFirstHeader(ACCEPT_DATETIME).getValue());
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
