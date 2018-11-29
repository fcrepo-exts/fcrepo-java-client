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
import static org.fcrepo.client.FedoraHeaderConstants.ACCEPT_DATETIME;
import static org.fcrepo.client.FedoraHeaderConstants.CACHE_CONTROL;
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
}
