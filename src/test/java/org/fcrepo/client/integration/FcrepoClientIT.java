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
package org.fcrepo.client.integration;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.GONE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PARTIAL_CONTENT;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION_FILENAME;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.ETAG;
import static org.fcrepo.client.FedoraHeaderConstants.LAST_MODIFIED;
import static org.fcrepo.client.FedoraHeaderConstants.STATE_TOKEN;
import static org.fcrepo.client.FedoraTypes.LDP_DIRECT_CONTAINER;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.fcrepo.client.TestUtils.sparqlUpdate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.EntityTag;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.fcrepo.client.ExternalContentHandling;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.fcrepo.client.FcrepoResponse;
import org.fcrepo.client.HeaderHelpers;
import org.jgroups.util.UUID;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author bbpennel
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-test/test-container.xml")
public class FcrepoClientIT extends AbstractResourceIT {

    protected URI url;

    final private String UNMODIFIED_DATE = "Mon, 1 Jan 2001 00:00:00 GMT";

    public FcrepoClientIT() throws Exception {
        super();

        client = FcrepoClient.client()
                .credentials("fedoraAdmin", "password")
                .authScope("localhost")
                .build();
    }

    @Before
    public void before() {
        url = URI.create(serverAddress + UUID.randomUUID().toString());
    }

    @Test
    public void testPost() throws Exception {
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .perform();

        assertEquals(CREATED.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPostBinary() throws Exception {
        final String slug = UUID.randomUUID().toString();
        final String filename = "hello.txt";
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .filename(filename)
                .slug(slug)
                .perform();

        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();

        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
        assertEquals("Location did not match slug", serverAddress + slug, response.getLocation().toString());

        assertNotNull("Didn't find linked description!", response.getLinkHeaders("describedby").get(0));

        final FcrepoResponse getResponse = client.get(response.getLocation()).perform();
        final Map<String, String> contentDisp = getResponse.getContentDisposition();
        assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

        assertEquals(mimetype, getResponse.getContentType());

        final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
        assertEquals(bodyContent, getContent);
    }

    @Test
    public void testPostExternalContent() throws Exception {
        final String filename = "hello.txt";
        final String mimetype = "text/plain";
        final String fileContent = "Hello post world";

        final Path contentPath = Files.createTempFile(null, ".txt");
        FileUtils.write(contentPath.toFile(), fileContent, "UTF-8");

        final FcrepoResponse response = client.post(new URI(serverAddress))
                .externalContent(contentPath.toUri(), mimetype, ExternalContentHandling.PROXY)
                .filename(filename)
                .perform();

        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();

        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);

        final FcrepoResponse getResponse = client.get(response.getLocation()).perform();
        final Map<String, String> contentDisp = getResponse.getContentDisposition();
        assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

        assertEquals(mimetype, getResponse.getContentType());

        final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
        assertEquals(fileContent, getContent);
    }

    @Test
    public void testPostDigestMismatch() throws Exception {
        final String bodyContent = "Hello world";
        final String invalidDigest = "adc83b19e793491b1c6ea0fd8b46cd9f32e592fc";

        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .digestSha1(invalidDigest)
                .perform();

        assertEquals("Invalid checksum was not rejected", CONFLICT.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPostDigestMultipleChecksums() throws Exception {
        final String bodyContent = "Hello world";

        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .digestMd5("3e25960a79dbc69b674cd4ec67a72c62")
                .digestSha1("7b502c3a1f48c8609ae212cdfb639dee39673f5e")
                .digestSha256("64ec88ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c")
                .perform();

        assertEquals("Checksums rejected", CREATED.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPostDigestMultipleChecksumsOneMismatch() throws Exception {
        final String bodyContent = "Hello world";

        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .digestMd5("3e25960a79dbc69b674cd4ec67a72c62")
                .digestSha1("7b502c3a1f48c8609ae212cdfb639dee39673f5e")
                // Incorrect sha256
                .digestSha256("123488ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c")
                .perform();

        assertEquals("Invalid checksum was not rejected", CONFLICT.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPostBinaryNullFilename() throws Exception {
        final String bodyContent = "Hello world";

        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .filename(null)
                .perform();

        assertEquals("Empty filename rejected", CREATED.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPostDirectContainer() throws Exception {
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .addInteractionModel(LDP_DIRECT_CONTAINER)
                .perform();

        final int status = response.getStatusCode();

        assertEquals("Didn't get a CREATED response!", CREATED.getStatusCode(), status);

        final FcrepoResponse getResponse = client.get(response.getLocation()).perform();
        assertTrue("Did not have ldp:DirectContainer type", getResponse.hasType(LDP_DIRECT_CONTAINER));
    }

    // FCREPO-3698
    @Test
    public void testPostBinaryFilenameSpecialCharacters() throws Exception {
        final String slug = UUID.randomUUID().toString();
        final String filename = "hello world\nof_we\0ird:+\tchar/acters\u0001.tx\rt";
        final String expectedFilename = "hello world of_we ird:+\tchar/acters .tx t";
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .filename(filename)
                .slug(slug)
                .perform();

        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();

        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
        assertEquals("Location did not match slug", serverAddress + slug, response.getLocation().toString());

        assertNotNull("Didn't find linked description!", response.getLinkHeaders("describedby").get(0));

        final FcrepoResponse getResponse = client.get(response.getLocation()).perform();
        final Map<String, String> contentDisp = getResponse.getContentDisposition();
        assertEquals(expectedFilename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

        assertEquals(mimetype, getResponse.getContentType());

        final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
        assertEquals(bodyContent, getContent);
    }

    @Test
    public void testPutBinaryFilenameSpecialCharacters() throws Exception {
        final String id = UUID.randomUUID().toString();
        final String filename = "hello world\nof_we\0ird:+\tchar/acters\u0001.tx\rt";
        final String expectedFilename = "hello world of_we ird:+\tchar/acters .tx t";
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse response = client.put(new URI(serverAddress + id))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .filename(filename)
                .perform();

        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();

        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);
        assertEquals("Location did not match slug", serverAddress + id, response.getLocation().toString());

        assertNotNull("Didn't find linked description!", response.getLinkHeaders("describedby").get(0));

        final FcrepoResponse getResponse = client.get(response.getLocation()).perform();
        final Map<String, String> contentDisp = getResponse.getContentDisposition();
        assertEquals(expectedFilename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

        assertEquals(mimetype, getResponse.getContentType());

        final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
        assertEquals(bodyContent, getContent);
    }

    @Test
    public void testPut() throws Exception {
        final FcrepoResponse response = create();

        assertEquals(CREATED.getStatusCode(), response.getStatusCode());
        assertEquals(url, response.getLocation());
    }

    @Test
    public void testPutEtag() throws Exception {
        // Create object
        final FcrepoResponse response = create();

        // Get the etag of the nearly created object
        final EntityTag etag = EntityTag.valueOf(response.getHeaderValue(ETAG));

        // Retrieve the body of the resource so we can modify it
        String body = getTurtle(url);
        body += "\n<> <http://purl.org/dc/elements/1.1/title> \"some-title\"";

        // Check that etag is making it through and being rejected
        final FcrepoResponse updateResp = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifMatch("\"bad-etag\"")
                .perform();

        assertEquals(PRECONDITION_FAILED.getStatusCode(), updateResp.getStatusCode());

        // Verify that etag is retrieved and resubmitted correctly
        final FcrepoResponse validResp = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifMatch("\"" + etag.getValue() + "\"")
                .perform();
        assertEquals(NO_CONTENT.getStatusCode(), validResp.getStatusCode());
    }

    @Test
    public void testPutUnmodifiedSince() throws Exception {
        // Create object
        final FcrepoResponse response = create();

        // Retrieve the body of the resource so we can modify it
        String body = getTurtle(url);
        body += "\n<> <http://purl.org/dc/elements/1.1/title> \"some-title\"";

        // Update the body the first time, which should succeed
        final String originalModified = response.getHeaderValue(LAST_MODIFIED);
        final FcrepoResponse matchResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifUnmodifiedSince(originalModified)
                .perform();

        assertEquals(NO_CONTENT.getStatusCode(), matchResponse.getStatusCode());

        // Update the triples a second time with old timestamp
        final FcrepoResponse mismatchResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifUnmodifiedSince(UNMODIFIED_DATE)
                .perform();

        assertEquals(PRECONDITION_FAILED.getStatusCode(), mismatchResponse.getStatusCode());
    }

    @Test
    public void testPutLenient() throws Exception {
        // Create object
        create();
        final String body = "<> <http://purl.org/dc/elements/1.1/title> \"some-title\"";

        // try to update without lenient header
        final FcrepoResponse strictResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .perform();
        assertEquals(CONFLICT.getStatusCode(), strictResponse.getStatusCode());

        // try again with lenient header
        final FcrepoResponse lenientResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .preferLenient()
                .perform();
        assertEquals(NO_CONTENT.getStatusCode(), lenientResponse.getStatusCode());
    }

    @Test
    public void testPutExternalContent() throws Exception {
        final String filename = "put.txt";
        final String mimetype = "text/plain";
        final String fileContent = "Hello put world";

        final Path contentPath = Files.createTempFile(null, ".txt");
        FileUtils.write(contentPath.toFile(), fileContent, "UTF-8");

        final FcrepoResponse response = client.put(url)
                .externalContent(contentPath.toUri(), mimetype, ExternalContentHandling.PROXY)
                .filename(filename)
                .perform();

        final String content = IOUtils.toString(response.getBody(), "UTF-8");
        final int status = response.getStatusCode();

        assertEquals("Didn't get a CREATED response! Got content:\n" + content,
                CREATED.getStatusCode(), status);

        final FcrepoResponse getResponse = client.get(response.getLocation()).perform();
        final Map<String, String> contentDisp = getResponse.getContentDisposition();
        assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

        assertEquals(mimetype, getResponse.getContentType());

        final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
        assertEquals(fileContent, getContent);
    }

    @Test
    public void testPutBinaryNullFilename() throws Exception {
        final String bodyContent = "Hello world";

        final FcrepoResponse response = client.put(url)
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .filename(null)
                .perform();

        assertEquals("Empty filename rejected", CREATED.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPatch() throws Exception {
        // Create object
        create();

        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        // Update triples with sparql update
        final FcrepoResponse response = client.patch(url)
                .body(body)
                .perform();

        assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testPatchEtagUpdated() throws Exception {
        // Create object
        final FcrepoResponse createResp = create();
        final EntityTag createdEtag = EntityTag.valueOf(createResp.getHeaderValue(ETAG));

        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());

        // Update triples with sparql update
        final FcrepoResponse response = client.patch(url)
                .body(body)
                .ifMatch("\"" + createdEtag.getValue() + "\"")
                .perform();

        final EntityTag updateEtag = EntityTag.valueOf(response.getHeaderValue(ETAG));

        assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        assertNotEquals("Etag did not change after patch", createdEtag, updateEtag);
    }

    @Test
    public void testPatchNoBody() throws Exception {
        create();

        final FcrepoResponse response = client.patch(url)
                .perform();

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testDelete() throws Exception {
        create();

        final FcrepoResponse response = client.delete(url).perform();

        assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

        assertEquals(GONE.getStatusCode(), client.get(url).perform().getStatusCode());
    }

    @Test
    public void testGet() throws Exception {
        create();
        final FcrepoResponse response = client.get(url).perform();

        assertEquals(OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testGetNotFound() throws Exception {
        final FcrepoResponse response = client.get(url).perform();

        assertEquals(NOT_FOUND.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testGetUnmodified() throws Exception {
        // Check that get returns a 304 if the item hasn't changed according to last-modified
        final FcrepoResponse response = create();

        // Get tomorrows date to provide as the modified-since date
        final String lastModified = response.getHeaderValue(LAST_MODIFIED);
        final Date modDate = DateUtils.parseDate(lastModified);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(modDate);
        cal.add(Calendar.DATE, 1);

        final FcrepoResponse modResp = client.get(url)
                .ifModifiedSince(DateUtils.formatDate(cal.getTime()))
                .perform();

        assertEquals(NOT_MODIFIED.getStatusCode(), modResp.getStatusCode());
        assertNull("Response body should not be returned when unmodified", modResp.getBody());
    }

    @Test
    public void testGetModified() throws Exception {
        // Check that get returns a 200 if the item has changed according to last-modified
        final FcrepoResponse response = create();

        // Get yesterdays date to provide as the modified-since date
        final String lastModified = response.getHeaderValue(LAST_MODIFIED);
        final Date modDate = DateUtils.parseDate(lastModified);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(modDate);
        cal.add(Calendar.DATE, -1);

        final FcrepoResponse modResp = client.get(url)
                .ifModifiedSince(DateUtils.formatDate(cal.getTime()))
                .perform();

        assertEquals(OK.getStatusCode(), modResp.getStatusCode());
        assertNotNull("GET response body should be normal when modified", modResp.getBody());
    }

    @Test
    public void testGetAccept() throws Exception {
        // Check that get returns a 304 if the item hasn't changed according to last-modified/etag
        create();

        final FcrepoResponse response = client.get(url)
                .accept("application/n-triples")
                .perform();

        assertEquals("application/n-triples", response.getHeaderValue(CONTENT_TYPE));
        assertEquals(OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testGetPrefer() throws Exception {
        // Check that get returns a 304 if the item hasn't changed according to last-modified/etag
        create();

        final FcrepoResponse response = client.get(url)
                .preferMinimal()
                .perform();

        assertEquals(OK.getStatusCode(), response.getStatusCode());
        assertEquals("return=minimal", response.getHeaderValue("Preference-Applied"));
    }

    @Test
    public void testGetRange() throws Exception {
        // Creating a binary for retrieval
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform();

        final URI url = response.getLocation();

        // Get the content of the object after the first 6 bytes
        final FcrepoResponse rangeResp = client.get(url)
                .range(6L, null)
                .perform();

        final String content = IOUtils.toString(rangeResp.getBody(), "UTF-8");
        assertEquals("Body did not contain correct range of original content", "world", content);
        assertEquals(PARTIAL_CONTENT.getStatusCode(), rangeResp.getStatusCode());
    }

    @Test
    public void testGetDisableRedirects() throws Exception {
        final String filename = "example.html";
        final String mimetype = "text/plain";

        final URI externalURI = URI.create("http://example.com/");

        final FcrepoResponse response = client.post(new URI(serverAddress))
                .externalContent(externalURI, mimetype, ExternalContentHandling.REDIRECT)
                .filename(filename)
                .perform();

        final FcrepoResponse getResponse = client.get(response.getLocation())
                .disableRedirects()
                .perform();
        assertEquals("Didn't get a REDIRECT response!",
                TEMPORARY_REDIRECT.getStatusCode(), getResponse.getStatusCode());
        assertEquals(mimetype, getResponse.getContentType());

        final Map<String, String> contentDisp = getResponse.getContentDisposition();
        assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

        assertEquals(externalURI, getResponse.getLocation());
    }

    @Test
    public void testGetWantDigest() throws Exception {
        // Creating a binary for retrieval
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform();

        final URI url = response.getLocation();

        // Request md5 digest with caching disabled
        final FcrepoResponse getResp = client.get(url)
                .wantDigest("md5")
                .noCache()
                .perform();
        assertEquals(OK.getStatusCode(), getResp.getStatusCode());

        final String digest = getResp.getHeaderValue(DIGEST);
        assertTrue("Did not contain md5", digest.contains("md5=3e25960a79dbc69b674cd4ec67a72c62"));
    }

    @Test
    public void testHead() throws Exception {
        final FcrepoResponse response = create();
        final FcrepoResponse headResp = client.head(url).perform();

        assertEquals(OK.getStatusCode(), headResp.getStatusCode());
        assertEquals(response.getHeaderValue(ETAG), headResp.getHeaderValue(ETAG));
        assertNotNull(headResp.getHeaderValue("Allow"));
    }

    @Test
    public void testHeadWantDigest() throws Exception {
        // Creating a binary for retrieval
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse response = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform();

        final URI url = response.getLocation();

        final Map<String, Double> qualityMap = new HashMap<>();
        qualityMap.put("md5", 1.0);
        qualityMap.put("sha", 1.0);
        qualityMap.put("sha-256", 0.4);

        final String wantDigest = HeaderHelpers.formatQualityValues(qualityMap);
        // Request multiple digests
        final FcrepoResponse getResp = client.head(url)
                .wantDigest(wantDigest)
                .perform();
        assertEquals(OK.getStatusCode(), getResp.getStatusCode());

        final String digest = getResp.getHeaderValue(DIGEST);
        assertTrue("Did not contain md5", digest.contains("md5=3e25960a79dbc69b674cd4ec67a72c62"));
        assertTrue("Did not contain sha1", digest.contains("sha=7b502c3a1f48c8609ae212cdfb639dee39673f5e"));
        assertTrue("Did not contain sha256", digest
                .contains("sha-256=64ec88ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c"));
    }

    @Test
    public void testOptions() throws Exception {
        create();
        final FcrepoResponse headResp = client.options(url).perform();

        assertEquals(OK.getStatusCode(), headResp.getStatusCode());
        assertNotNull(headResp.getHeaderValue("Allow"));
        assertNotNull(headResp.getHeaderValue("Accept-Post"));
        assertNotNull(headResp.getHeaderValue("Accept-Patch"));
    }

    @Test
    public void testMove() throws Exception {
        create();

        final URI destUrl = new URI(url.toString() + "_dest");
        final FcrepoResponse moveResp = client.move(url, destUrl).perform();
        assertEquals(CREATED.getStatusCode(), moveResp.getStatusCode());

        assertEquals("Object still at original url",
                GONE.getStatusCode(), client.get(url).perform().getStatusCode());

        assertEquals("Object not at expected new url",
                OK.getStatusCode(), client.get(destUrl).perform().getStatusCode());
    }

    @Test
    public void testCopy() throws Exception {
        create();

        // Add something identifiable to the record
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        client.patch(url).body(body).perform();

        final URI destUrl = new URI(url.toString() + "_dest");
        final FcrepoResponse copyResp = client.copy(url, destUrl).perform();
        assertEquals(CREATED.getStatusCode(), copyResp.getStatusCode());

        final FcrepoResponse originalResp = client.get(url).perform();
        final String originalContent = IOUtils.toString(originalResp.getBody(), "UTF-8");
        assertTrue(originalContent.contains("Foo"));

        final FcrepoResponse destResp = client.get(destUrl).perform();
        final String destContent = IOUtils.toString(destResp.getBody(), "UTF-8");
        assertTrue(destContent.contains("Foo"));
    }

    @Ignore("Pending state token implementation in fcrepo")
    @Test
    public void testStateTokens() throws Exception {
        create();

        final FcrepoResponse getResp = client.get(url).perform();
        final String stateToken = getResp.getHeaderValue(STATE_TOKEN);

        // Attempt to update triples with an incorrect state token
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        final FcrepoResponse badTokenResp = client.patch(url)
                .body(body)
                .ifStateToken("bad_state_token")
                .perform();
        assertEquals(PRECONDITION_FAILED.getStatusCode(), badTokenResp.getStatusCode());

        // Update triples with the correct state token
        body.reset();
        final FcrepoResponse goodTokenResp = client.patch(url)
                .body(body)
                .ifStateToken(stateToken)
                .perform();
        assertEquals(NO_CONTENT.getStatusCode(), goodTokenResp.getStatusCode());
    }

    private FcrepoResponse create() throws FcrepoOperationFailedException {
        return client.put(url).perform();
    }

    private String getTurtle(final URI url) throws Exception {
        final FcrepoResponse getResponse = client.get(url)
                .accept("text/turtle")
                .perform();
        return IOUtils.toString(getResponse.getBody(), "UTF-8");
    }
}
