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

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author acoburn
 */
@RunWith(MockitoJUnitRunner.class)
public class FcrepoLinkTest {

    @Test
    public void testLink() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s>; rel=\"%s\"", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(URI.create(url), link.getUri());
        assertEquals(url, link.getUri().toString());
        assertEquals(rel, link.getRel());
    }

    @Test
    public void testLinkNoQuotes() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s>; rel=%s", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(URI.create(url), link.getUri());
        assertEquals(url, link.getUri().toString());
        assertEquals(rel, link.getRel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkNoBrackets() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("%s; rel=%s", url, rel);
        new FcrepoLink(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkBadBrackets1() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s; rel=%s", url, rel);
        new FcrepoLink(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkBadBrackets2() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("%s>; rel=%s", url, rel);
        new FcrepoLink(header);
    }

    @Test
    public void testLinkBadQuotes() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s>; rel=\"%s", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(URI.create(url), link.getUri());
        assertNull("Incorrectly quoted parameter should return null", link.getRel());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNullLink() {
        new FcrepoLink(null);
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
                .uri("http://example.com/")
                .rel("bar")
                .type("foo")
                .param("special", "val")
                .build();

        assertEquals(URI.create("http://example.com/"), link.getUri());
        assertEquals("bar", link.getRel());
        assertEquals("foo", link.getType());
        assertEquals("val", link.getParam("special"));
    }

    @Test
    public void testBuilderParamNeedingQuotes() {

    }

    @Test
    public void testToStringNoParams() {
        final String url = "http://localhost/rest/a/b/c";
        final String header = String.format("<%s>", url);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(header, link.toString());
    }

    @Test
    public void testToStringWithParam() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s>; rel=\"%s\"", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(header, link.toString());
    }

    @Test
    public void testToStringMultipleParams() {
        final FcrepoLink link = new FcrepoLink.Builder()
                .uri("http://example.com/")
                .rel("bar")
                .type("foo")
                .param("special", "val")
                .build();

        final String header = link.toString();
        assertTrue("Stringified link did not contain URI", header.contains("<http://example.com/>"));
        assertTrue("Stringified link did not contain rel", header.contains("; rel=\"bar\""));
        assertTrue("Stringified link did not contain type", header.contains("; type=\"foo\""));
        assertTrue("Stringified link did not contain param", header.contains("; special=\"val\""));
    }
}
