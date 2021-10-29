/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.fcrepo.client.FedoraHeaderConstants.MEMENTO_DATETIME;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

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
public class HistoricMementoBuilderTest {

    private final String HISTORIC_DATETIME =
            UTC_RFC_1123_FORMATTER.format(LocalDateTime.of(2000, 1, 1, 0, 0).atZone(ZoneOffset.UTC));

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

    private HistoricMementoBuilder testBuilder;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);

        uri = create(baseUrl);
    }

    @Test
    public void testCreateWithInstantHeader() throws Exception {
        final Instant datetime = LocalDateTime.of(2000, 1, 1, 00, 00).atZone(ZoneOffset.UTC).toInstant();

        testBuilder = new HistoricMementoBuilder(uri, client, datetime);
        testBuilder.perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals(HISTORIC_DATETIME, request.getFirstHeader(MEMENTO_DATETIME).getValue());
    }

    @Test
    public void testCreateWithStringMementoDatetime() throws Exception {
        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals(HISTORIC_DATETIME, request.getFirstHeader(MEMENTO_DATETIME).getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateWithNoDatetime() throws Exception {
        testBuilder = new HistoricMementoBuilder(uri, client, (String) null);
        testBuilder.perform();
    }

    @Test(expected = DateTimeParseException.class)
    public void testCreateWithNonRFC1123() throws Exception {
        final String mementoDatetime = Instant.now().toString();
        testBuilder = new HistoricMementoBuilder(uri, client, mementoDatetime);
        testBuilder.perform();
    }
}
