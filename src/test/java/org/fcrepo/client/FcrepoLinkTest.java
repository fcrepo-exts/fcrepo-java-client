/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Test
    public void testLinkNoBrackets() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("%s; rel=%s", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(null, link.getUri());
        assertEquals(null, link.getRel());
    }

    @Test
    public void testLinkBadBrackets1() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s; rel=%s", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(null, link.getUri());
        assertEquals(null, link.getRel());
    }

    @Test
    public void testLinkBadBrackets2() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("%s>; rel=%s", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(null, link.getUri());
        assertEquals(null, link.getRel());
    }

    @Test
    public void testLinkBadQuotes() {
        final String url = "http://localhost/rest/a/b/c";
        final String rel = "describedby";
        final String header = String.format("<%s>; rel=\"%s", url, rel);
        final FcrepoLink link = new FcrepoLink(header);
        assertEquals(URI.create(url), link.getUri());
        assertEquals("\"" + rel, link.getRel());
    }


    @Test
    public void testNullLink() {
        final FcrepoLink link = new FcrepoLink(null);
        assertEquals(null, link.getUri());
        assertEquals(null, link.getRel());
    }

    @Test
    public void testMultipleSegments() {
        final FcrepoLink link = new FcrepoLink("<a>; rel=foo; rel=bar");
        assertEquals(null, link.getUri());
        assertEquals(null, link.getRel());
    }

    @Test
    public void testMultipleMeta() {
        final FcrepoLink link = new FcrepoLink("<a>; rel=foo=bar");
        assertEquals(URI.create("a"), link.getUri());
        assertEquals(null, link.getRel());
    }

    @Test
    public void testNotMetaRel() {
        final FcrepoLink link = new FcrepoLink("<a>; foo=bar");
        assertEquals(URI.create("a"), link.getUri());
        assertEquals(null, link.getRel());
    }
}
