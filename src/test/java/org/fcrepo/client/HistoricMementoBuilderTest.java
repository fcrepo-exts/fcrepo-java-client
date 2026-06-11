/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.net.URI.create;
import static org.fcrepo.client.ExternalContentHandling.PROXY;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.FedoraHeaderConstants.SLUG;
import static org.fcrepo.client.FedoraTypes.LDP_DIRECT_CONTAINER;
import static org.fcrepo.client.LinkHeaderConstants.ACL_REL;
import static org.fcrepo.client.LinkHeaderConstants.EXTERNAL_CONTENT_HANDLING;
import static org.fcrepo.client.LinkHeaderConstants.EXTERNAL_CONTENT_REL;
import static org.fcrepo.client.LinkHeaderConstants.TYPE_REL;
import static org.fcrepo.client.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.fcrepo.client.FedoraHeaderConstants.MEMENTO_DATETIME;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

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

    @Test
    public void testWithBody() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.body(bodyStream, "plain/text")
                .digestSha1("checksum")
                .filename("file.txt")
                .slug("my-slug")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());

        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        final HttpEntity bodyEntity = request.getEntity();
        assertEquals(bodyStream, bodyEntity.getContent());
        assertEquals("plain/text", request.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals("sha=checksum", request.getFirstHeader(DIGEST).getValue());
        assertEquals("attachment; filename=\"file.txt\"", request.getFirstHeader(CONTENT_DISPOSITION).getValue());
        assertEquals("my-slug", request.getFirstHeader(SLUG).getValue());
    }

    @Test
    public void testBodyStreamDefaultType() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.body(bodyStream).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) requestCaptor.getValue();
        assertEquals(bodyStream, request.getEntity().getContent());
    }

    @Test
    public void testDigestVariants() throws Exception {
        final InputStream bodyStream = mock(InputStream.class);

        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.body(bodyStream, "plain/text")
                .digestMd5("md5sum")
                .digestSha256("sha256sum")
                .digestSha512("sha512sum")
                .digest("shasum", "sha")
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals("md5=md5sum, sha256=sha256sum, sha512=sha512sum, sha=shasum",
                request.getFirstHeader(DIGEST).getValue());
    }

    @Test
    public void testExternalContent() throws Exception {
        final URI contentURI = URI.create("file:///path/to/file");

        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.externalContent(contentURI, "plain/text", PROXY).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();

        final FcrepoLink extLink = new FcrepoLink(request.getFirstHeader(LINK).getValue());
        assertEquals(EXTERNAL_CONTENT_REL, extLink.getRel());
        assertEquals(PROXY, extLink.getParams().get(EXTERNAL_CONTENT_HANDLING));
        assertEquals("plain/text", extLink.getType());
    }

    @Test
    public void testAddInteractionModel() throws Exception {
        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.addInteractionModel(LDP_DIRECT_CONTAINER).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final FcrepoLink interLink = new FcrepoLink(requestCaptor.getValue().getFirstHeader(LINK).getValue());
        assertEquals(TYPE_REL, interLink.getRel());
        assertEquals(LDP_DIRECT_CONTAINER, interLink.getUri().toString());
    }

    @Test
    public void testLinkAcl() throws Exception {
        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.linkAcl("http://localhost/acl").perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final FcrepoLink aclLink = new FcrepoLink(requestCaptor.getValue().getFirstHeader(LINK).getValue());
        assertEquals(ACL_REL, aclLink.getRel());
        assertEquals("http://localhost/acl", aclLink.getUri().toString());
    }

    @Test
    public void testAddHeaderAndLinkHeader() throws Exception {
        final FcrepoLink link = FcrepoLink.fromUri("http://example.com/link").type("foo").build();

        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.addHeader("my-header", "head-val")
                .addLinkHeader(link)
                .perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        assertEquals("head-val", request.getFirstHeader("my-header").getValue());
        assertEquals(link.toString(), request.getFirstHeader(LINK).getValue());
    }

    @Test
    public void testNullSlugAndFilename() throws Exception {
        testBuilder = new HistoricMementoBuilder(uri, client, HISTORIC_DATETIME);
        testBuilder.slug(null).filename(null).perform();

        verify(client).executeRequest(eq(uri), requestCaptor.capture());
        final HttpRequestBase request = requestCaptor.getValue();
        // a null slug adds no header; a null filename still produces a bare attachment disposition
        assertEquals(null, request.getFirstHeader(SLUG));
        assertEquals("attachment", request.getFirstHeader(CONTENT_DISPOSITION).getValue());
    }
}
