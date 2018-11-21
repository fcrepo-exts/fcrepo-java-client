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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.fcrepo.client.LinkHeaderConstants.DESCRIBEDBY_REL;
import static org.fcrepo.client.LinkHeaderConstants.MEMENTO_ORIGINAL_REL;
import static org.fcrepo.client.LinkHeaderConstants.MEMENTO_TIME_MAP_REL;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author acoburn
 */
@RunWith(MockitoJUnitRunner.class)
public class FcrepoLinkTest {

    private static final String TEST_URI = "http://localhost/rest/a/b/c";

    private static final String MULTI_LINK_HEADER =
            "<http://a.example.org/>; rel=\"original\", " +
            "<http://arxiv.example.net/timemap/http://a.example.org/>" +
            "; rel=\"timemap\"; type=\"application/link-format\"" +
            "; from=\"Tue, 15 Sep 2000 11:28:26 GMT\"" +
            "; until=\"Wed, 20 Jan 2010 09:34:33 GMT\"";

    @Test
    public void testLink() {
        final String header = String.format("<%s>; rel=\"%s\"", TEST_URI, DESCRIBEDBY_REL);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(URI.create(TEST_URI), link.getUri());
        assertEquals(TEST_URI, link.getUri().toString());
        assertEquals(DESCRIBEDBY_REL, link.getRel());
    }

    @Test
    public void testLinkNoQuotes() {
        final String header = String.format("<%s>; rel=%s", TEST_URI, DESCRIBEDBY_REL);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(URI.create(TEST_URI), link.getUri());
        assertEquals(TEST_URI, link.getUri().toString());
        assertEquals(DESCRIBEDBY_REL, link.getRel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkNoBrackets() {
        final String header = String.format("%s; rel=%s", TEST_URI, DESCRIBEDBY_REL);
        new FcrepoLink(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkBadBrackets1() {
        final String header = String.format("<%s; rel=%s", TEST_URI, DESCRIBEDBY_REL);
        new FcrepoLink(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkBadBrackets2() {
        final String header = String.format("%s>; rel=%s", TEST_URI, DESCRIBEDBY_REL);
        new FcrepoLink(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkBadQuotes() {
        final String header = String.format("<%s>; rel=\"%s", TEST_URI, DESCRIBEDBY_REL);
        new FcrepoLink(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullLink() {
        new FcrepoLink(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyLink() {
        new FcrepoLink(" ");
    }

    @Test
    public void testMultipleSegments() {
        final FcrepoLink link = new FcrepoLink("<a>; rel=foo; bar=bar");
        assertEquals(URI.create("a"), link.getUri());
        assertEquals("foo", link.getRel());
        assertEquals("bar", link.getParam("bar"));
    }

    @Test
    public void testMultipleMeta() {
        final FcrepoLink link = new FcrepoLink("<a>; rel=foo=bar");
        assertEquals(URI.create("a"), link.getUri());
        // = is an allowable character for the parameter value
        assertEquals("foo=bar", link.getRel());
    }

    @Test
    public void testArbitraryParameter() {
        final FcrepoLink link = new FcrepoLink("<a>; foo=bar");
        assertEquals(URI.create("a"), link.getUri());
        assertNull(link.getRel());
    }

    @Test
    public void testQuotedParamWithDelimiters() {
        final FcrepoLink link = new FcrepoLink("<a>; foo=\"a,b;c=d\"");
        assertEquals(URI.create("a"), link.getUri());
        assertEquals("a,b;c=d", link.getParam("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnquotedParamWithDelimiters() {
        new FcrepoLink("<a>; foo=a;b");
    }

    @Test
    public void testNoParameters() {
        final FcrepoLink link = new FcrepoLink("<a>");
        assertEquals(URI.create("a"), link.getUri());
        assertNull(link.getRel());
        assertNull(link.getType());
    }

    @Test
    public void testFromUriString() {
        final FcrepoLink link = FcrepoLink.fromUri("http://example.com/").build();
        assertEquals(URI.create("http://example.com/"), link.getUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromUriStringNull() {
        FcrepoLink.fromUri((String) null).build();
    }

    @Test
    public void testFromUri() {
        final FcrepoLink link = FcrepoLink.fromUri(URI.create("http://example.com/")).build();
        assertEquals(URI.create("http://example.com/"), link.getUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromUriNull() {
        FcrepoLink.fromUri((URI) null).build();
    }

    @Test
    public void testBuilder() {
        final FcrepoLink link = new FcrepoLink.Builder()
                .uri(TEST_URI)
                .rel("bar")
                .type("foo")
                .param("special", "val")
                .build();

        assertEquals(URI.create(TEST_URI), link.getUri());
        assertEquals("bar", link.getRel());
        assertEquals("foo", link.getType());
        assertEquals("val", link.getParam("special"));
    }

    @Test
    public void testToStringNoParams() {
        final String header = String.format("<%s>", TEST_URI);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(header, link.toString());
    }

    @Test
    public void testToStringWithParam() {
        final String header = String.format("<%s>; rel=\"%s\"", TEST_URI, DESCRIBEDBY_REL);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(header, link.toString());
    }

    @Test
    public void testToStringMultipleParams() {
        final FcrepoLink link = new FcrepoLink.Builder()
                .uri(TEST_URI)
                .rel("bar")
                .type("foo")
                .param("special", "val")
                .build();

        final String header = link.toString();
        assertTrue("Stringified link did not contain URI", header.contains(TEST_URI));
        assertTrue("Stringified link did not contain rel", header.contains("; rel=\"bar\""));
        assertTrue("Stringified link did not contain type", header.contains("; type=\"foo\""));
        assertTrue("Stringified link did not contain param", header.contains("; special=\"val\""));
    }

    @Test
    public void testValueOf() {
        final String header = String.format("<%s>; rel=\"%s\"", TEST_URI, DESCRIBEDBY_REL);

        final FcrepoLink link = FcrepoLink.valueOf(header);

        assertEquals(TEST_URI, link.getUri().toString());
        assertEquals(DESCRIBEDBY_REL, link.getRel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfWithComma() {
        FcrepoLink.valueOf(MULTI_LINK_HEADER);
    }

    @Test
    public void testFromHeaderSingleLink() {
        final String header = String.format("<%s>; rel=\"%s\"", TEST_URI, DESCRIBEDBY_REL);

        final List<FcrepoLink> links = FcrepoLink.fromHeader(header);
        assertEquals("Incorrect number of links returned", 1, links.size());

        assertEquals(TEST_URI, links.get(0).getUri().toString());
        assertEquals(DESCRIBEDBY_REL, links.get(0).getRel());
    }

    @Test
    public void testFromHeaderMultipleLinks() {
        final List<FcrepoLink> links = FcrepoLink.fromHeader(MULTI_LINK_HEADER);
        assertEquals("Incorrect number of links returned", 2, links.size());

        final FcrepoLink link1 = links.get(0);
        assertEquals("http://a.example.org/", link1.getUri().toString());
        assertEquals(MEMENTO_ORIGINAL_REL, link1.getRel());

        final FcrepoLink link2 = links.get(1);
        assertEquals("http://arxiv.example.net/timemap/http://a.example.org/", link2.getUri().toString());
        assertEquals(MEMENTO_TIME_MAP_REL, link2.getRel());
        assertEquals("application/link-format", link2.getType());
        assertEquals("Tue, 15 Sep 2000 11:28:26 GMT", link2.getParam("from"));
    }

    @Test
    public void testFromHeaderUriContainsComma() throws Exception {
        final String header = String.format("<a,b>; rel=\"%s\"", DESCRIBEDBY_REL);

        final List<FcrepoLink> links = FcrepoLink.fromHeader(header);
        assertEquals("Incorrect number of links returned", 1, links.size());

        assertEquals("a,b", links.get(0).getUri().toString());
        assertEquals(DESCRIBEDBY_REL, links.get(0).getRel());
    }

    @Test
    public void testFromHeaderParamContainsComma() throws Exception {
        final String header = "<a>; param=\"value,with,commas\"";

        final List<FcrepoLink> links = FcrepoLink.fromHeader(header);
        assertEquals("Incorrect number of links returned", 1, links.size());

        assertEquals("a", links.get(0).getUri().toString());
        assertEquals("value,with,commas", links.get(0).getParam("param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromHeaderUnterminatedUri() {
        final String header = String.format("<a; rel=\"%s\"", DESCRIBEDBY_REL);
        FcrepoLink.fromHeader(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromHeaderUnterminatedQuotes() {
        final String header = String.format("<a>; rel=\"%s, <b>", DESCRIBEDBY_REL);
        FcrepoLink.fromHeader(header);
    }
}
