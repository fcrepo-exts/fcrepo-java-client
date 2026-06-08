/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.FedoraHeaderConstants.ATOMIC_ID;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.IF_MATCH;
import static org.fcrepo.client.FedoraHeaderConstants.IF_STATE_TOKEN;
import static org.fcrepo.client.FedoraHeaderConstants.IF_UNMODIFIED_SINCE;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
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
public class PatchBuilderTest {

    @Mock
    private FcrepoClient client;

    @Mock
    private FcrepoResponse fcrepoResponse;

    @Captor
    private ArgumentCaptor<HttpRequestBase> requestCaptor;

    private PatchBuilder testBuilder;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        when(client.executeRequest(any(URI.class), any(HttpRequestBase.class)))
                .thenReturn(fcrepoResponse);

        uri = create(baseUrl);
        testBuilder = new PatchBuilder(uri, client);
    }

    @Test
    public void testWithBodyDefaultType() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("application/sparql-update", request.getFirstHeader(CONTENT_TYPE).getValue());
    }

    @Test
    public void testWithBodyCustomType() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream, "text/plain")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());

        assertEquals("text/plain", request.getFirstHeader(CONTENT_TYPE).getValue());
    }

    @Test
    public void testStateToken() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);
        final String token = "state";

        testBuilder.body(bodyStream)
                .ifStateToken(token)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals(token, request.getFirstHeader(IF_STATE_TOKEN).getValue());
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

    @Test
    public void testWithModificationHeaders() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);
        final String etag = "123456";
        final String lastModified = "Mon, 19 May 2014 19:44:59 GMT";

        testBuilder.body(bodyStream)
                .ifMatch(etag)
                .ifUnmodifiedSince(lastModified)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals(etag, request.getFirstHeader(IF_MATCH).getValue());
        assertEquals(lastModified, request.getFirstHeader(IF_UNMODIFIED_SINCE).getValue());
    }

    @Test
    public void testDigests() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream)
                .digestMd5("md5sum")
                .digestSha1("sha1sum")
                .digestSha256("sha256sum")
                .digestSha512("sha512sum")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final String digestHeader = requestCaptor.getValue().getFirstHeader(DIGEST).getValue();
        assertEquals("md5=md5sum, sha=sha1sum, sha256=sha256sum, sha512=sha512sum", digestHeader);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedDigest() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream)
                .digest("checksum")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals("sha=checksum", requestCaptor.getValue().getFirstHeader(DIGEST).getValue());
    }

    @Test
    public void testDigestWithAlg() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder.body(bodyStream)
                .digest("checksum", "sha512")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals("sha512=checksum", requestCaptor.getValue().getFirstHeader(DIGEST).getValue());
    }

    @Test
    public void testAddTransaction() throws Exception {
        final URI txUri = create("http://localhost:8080/rest/tx:1234");
        testBuilder.addTransaction(txUri).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        assertEquals(txUri.toString(), requestCaptor.getValue().getFirstHeader(ATOMIC_ID).getValue());
    }
}
