/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client.integration;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.GONE;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NOT_MODIFIED;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.PARTIAL_CONTENT;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static jakarta.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION_FILENAME;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DIGEST;
import static org.fcrepo.client.FedoraHeaderConstants.ETAG;
import static org.fcrepo.client.FedoraHeaderConstants.LAST_MODIFIED;
import static org.fcrepo.client.FedoraHeaderConstants.STATE_TOKEN;
import static org.fcrepo.client.FedoraTypes.LDP_DIRECT_CONTAINER;
import static org.fcrepo.client.TestUtils.TEXT_TURTLE;
import static org.fcrepo.client.TestUtils.sparqlUpdate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.EntityTag;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.fcrepo.client.ExternalContentHandling;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.fcrepo.client.FcrepoResponse;
import org.fcrepo.client.HeaderHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author bbpennel
 */
public class FcrepoClientIT extends AbstractResourceIT {

    private static FcrepoClient client;

    private URI url;

    final private String UNMODIFIED_DATE = "Mon, 1 Jan 2001 00:00:00 GMT";

    @BeforeAll
    public static void beforeClass() {
        client = FcrepoClient.client()
                .credentials("fedoraAdmin", "fedoraAdmin")
                .authScope("localhost")
                .build();
    }

    @AfterAll
    public static void afterClass() throws IOException {
        client.close();
    }

    @BeforeEach
    public void before() {
        url = URI.create(SERVER_ADDRESS + UUID.randomUUID().toString());
    }

    @Test
    public void testPost() throws Exception {
        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS)).perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testPostBinary() throws Exception {
        final String slug = UUID.randomUUID().toString();
        final String filename = "hello.txt";
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";

        final URI createdResourceUri;
        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .filename(filename)
                .slug(slug)
                .perform()) {

            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            createdResourceUri = response.getLocation();

            assertEquals(CREATED.getStatusCode(), status, "Didn't get a CREATED response! Got content:\n" + content);
            assertEquals(SERVER_ADDRESS + slug, createdResourceUri.toString(), "Location did not match slug");

            assertNotNull(response.getLinkHeaders("describedby").get(0), "Didn't find linked description!");
        }

        try (final FcrepoResponse getResponse = client.get(createdResourceUri).perform()) {
            final Map<String, String> contentDisp = getResponse.getContentDisposition();
            assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

            assertEquals(mimetype, getResponse.getContentType());

            final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
            assertEquals(bodyContent, getContent);
        }
    }

    @Test
    public void testPostExternalContent() throws Exception {
        final String filename = "hello.txt";
        final String mimetype = "text/plain";
        final String fileContent = "Hello post world";

        final Path contentPath = Files.createTempFile(null, ".txt");
        FileUtils.write(contentPath.toFile(), fileContent, "UTF-8");

        final URI createdResourceUri;
        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .externalContent(contentPath.toUri(), mimetype, ExternalContentHandling.PROXY)
                .filename(filename)
                .perform()) {

            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            createdResourceUri = response.getLocation();

            assertEquals(CREATED.getStatusCode(), status, "Didn't get a CREATED response! Got content:\n" + content);
        }

        try (final FcrepoResponse getResponse = client.get(createdResourceUri).perform()) {
            final Map<String, String> contentDisp = getResponse.getContentDisposition();
            assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

            assertEquals(mimetype, getResponse.getContentType());

            final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
            assertEquals(fileContent, getContent);
        }
    }

    @Test
    public void testPostDigestMismatch() throws Exception {
        final String bodyContent = "Hello world";
        final String invalidDigest = "adc83b19e793491b1c6ea0fd8b46cd9f32e592fc";

        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .digestSha1(invalidDigest)
                .perform()) {

            assertEquals(CONFLICT.getStatusCode(), response.getStatusCode(), "Invalid checksum was not rejected");
        }
    }

    @Test
    public void testPostDigestMultipleChecksums() throws Exception {
        final String bodyContent = "Hello world";

        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .digestMd5("3e25960a79dbc69b674cd4ec67a72c62")
                .digestSha1("7b502c3a1f48c8609ae212cdfb639dee39673f5e")
                .digestSha256("64ec88ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c")
                .perform()) {

            assertEquals(CREATED.getStatusCode(), response.getStatusCode(), "Checksums rejected");
        }
    }

    @Test
    public void testPostDigestMultipleChecksumsOneMismatch() throws Exception {
        final String bodyContent = "Hello world";

        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .digestMd5("3e25960a79dbc69b674cd4ec67a72c62")
                .digestSha1("7b502c3a1f48c8609ae212cdfb639dee39673f5e")
                // Incorrect sha256
                .digestSha256("123488ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c")
                .perform()) {

            assertEquals(CONFLICT.getStatusCode(), response.getStatusCode(), "Invalid checksum was not rejected");
        }
    }

    @Test
    public void testPostBinaryNullFilename() throws Exception {
        final String bodyContent = "Hello world";

        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .filename(null)
                .perform()) {

            assertEquals(CREATED.getStatusCode(), response.getStatusCode(), "Empty filename rejected");
        }
    }

    @Test
    public void testPostDirectContainer() throws Exception {
        final URI createdResourceUri;
        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .addInteractionModel(LDP_DIRECT_CONTAINER)
                .perform()) {

            final int status = response.getStatusCode();
            createdResourceUri = response.getLocation();

            assertEquals(CREATED.getStatusCode(), status, "Didn't get a CREATED response!");
        }

        try (final FcrepoResponse getResponse = client.get(createdResourceUri).perform()) {
            assertTrue(getResponse.hasType(LDP_DIRECT_CONTAINER), "Did not have ldp:DirectContainer type");
        }
    }

    @Test
    public void testPostBinaryFilenameSpecialCharacters() throws Exception {
        final String slug = UUID.randomUUID().toString();
        final String filename = "hello world\nof_weird:+\tchar/acters.tx\rt";
        final String expectedFilename = "hello world of_weird:+\tchar/acters.tx t";
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";

        final URI createdResourceUri;
        try (final FcrepoResponse response =
                client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .filename(filename)
                .slug(slug)
                .perform()) {

            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            createdResourceUri = response.getLocation();

            assertEquals(CREATED.getStatusCode(), status, "Didn't get a CREATED response! Got content:\n" + content);
            assertEquals(SERVER_ADDRESS + slug, response.getLocation().toString(), "Location did not match slug");

            assertNotNull(response.getLinkHeaders("describedby").get(0), "Didn't find linked description!");
        }

        try (final FcrepoResponse getResponse = client.get(createdResourceUri).perform()) {
            final Map<String, String> contentDisp = getResponse.getContentDisposition();
            assertEquals(expectedFilename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

            assertEquals(mimetype, getResponse.getContentType());

            final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
            assertEquals(bodyContent, getContent);
        }
    }

    @Test
    public void testPutBinaryFilenameSpecialCharacters() throws Exception {
        final String id = UUID.randomUUID().toString();
        final String filename = "hello world\nof_weird:+\tchar/acters.tx\rt";
        final String expectedFilename = "hello world of_weird:+\tchar/acters.tx t";
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";

        final URI createdResourceUri;
        try (final FcrepoResponse response =
                client.put(new URI(SERVER_ADDRESS + id))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .filename(filename)
                .perform()) {

            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            createdResourceUri = response.getLocation();

            assertEquals(CREATED.getStatusCode(), status, "Didn't get a CREATED response! Got content:\n" + content);
            assertEquals(SERVER_ADDRESS + id, response.getLocation().toString(), "Location did not match slug");

            assertNotNull(response.getLinkHeaders("describedby").get(0), "Didn't find linked description!");
        }

        try (final FcrepoResponse getResponse = client.get(createdResourceUri).perform()) {
            final Map<String, String> contentDisp = getResponse.getContentDisposition();
            assertEquals(expectedFilename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

            assertEquals(mimetype, getResponse.getContentType());

            final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
            assertEquals(bodyContent, getContent);
        }
    }

    @Test
    public void testPut() throws Exception {
        try (final FcrepoResponse response = create()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode());
            assertEquals(url, response.getLocation());
        }
    }

    @Test
    public void testPutEtag() throws Exception {
        // Create object
        final EntityTag etag;
        try (final FcrepoResponse response = create()) {
            // Get the etag of the nearly created object
            etag = EntityTag.valueOf(response.getHeaderValue(ETAG));
        }

        // Retrieve the body of the resource so we can modify it
        String body = getTurtle(url);
        body += "\n<> <http://purl.org/dc/elements/1.1/title> \"some-title\"";

        // Check that etag is making it through and being rejected
        try (final FcrepoResponse updateResp = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifMatch("\"bad-etag\"")
                .perform()) {

            assertEquals(PRECONDITION_FAILED.getStatusCode(), updateResp.getStatusCode());
        }

        // Verify that etag is retrieved and resubmitted correctly
        try (final FcrepoResponse validResp = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifMatch("\"" + etag.getValue() + "\"")
                .perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), validResp.getStatusCode());
        }
    }

    @Test
    public void testPutUnmodifiedSince() throws Exception {
        // Create object
        final String originalModified;
        try (final FcrepoResponse response = create()) {
            originalModified = response.getHeaderValue(LAST_MODIFIED);
        }

        // Retrieve the body of the resource so we can modify it
        String body = getTurtle(url);
        body += "\n<> <http://purl.org/dc/elements/1.1/title> \"some-title\"";

        // Update the body the first time, which should succeed
        try (final FcrepoResponse matchResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifUnmodifiedSince(originalModified)
                .perform()) {

            assertEquals(NO_CONTENT.getStatusCode(), matchResponse.getStatusCode());
        }

        // Update the triples a second time with old timestamp
        try (final FcrepoResponse mismatchResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .ifUnmodifiedSince(UNMODIFIED_DATE)
                .perform()) {

            assertEquals(PRECONDITION_FAILED.getStatusCode(), mismatchResponse.getStatusCode());
        }
    }

    @Test
    public void testPutLenient() throws Exception {
        // Create object
        try (final FcrepoResponse response = create()) {
        }

        final String body = "<> <http://purl.org/dc/elements/1.1/title> \"some-title\"";

        // try to update without lenient header
        try (final FcrepoResponse strictResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), strictResponse.getStatusCode());
        }

        // try again with lenient header
        try (final FcrepoResponse lenientResponse = client.put(url)
                .body(new ByteArrayInputStream(body.getBytes()), TEXT_TURTLE)
                .preferLenient()
                .perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), lenientResponse.getStatusCode());
        }
    }

    @Test
    public void testPutExternalContent() throws Exception {
        final String filename = "put.txt";
        final String mimetype = "text/plain";
        final String fileContent = "Hello put world";

        final Path contentPath = Files.createTempFile(null, ".txt");
        FileUtils.write(contentPath.toFile(), fileContent, "UTF-8");

        final URI createdResourceUri;
        try (final FcrepoResponse response = client.put(url)
                .externalContent(contentPath.toUri(), mimetype, ExternalContentHandling.PROXY)
                .filename(filename)
                .perform()) {

            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            final int status = response.getStatusCode();
            createdResourceUri = response.getLocation();

            assertEquals(CREATED.getStatusCode(), status, "Didn't get a CREATED response! Got content:\n" + content);
        }

        try (final FcrepoResponse getResponse = client.get(createdResourceUri).perform()) {
            final Map<String, String> contentDisp = getResponse.getContentDisposition();
            assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

            assertEquals(mimetype, getResponse.getContentType());

            final String getContent = IOUtils.toString(getResponse.getBody(), "UTF-8");
            assertEquals(fileContent, getContent);
        }
    }

    @Test
    public void testPutBinaryNullFilename() throws Exception {
        final String bodyContent = "Hello world";

        try (final FcrepoResponse response = client.put(url)
                .body(new ByteArrayInputStream(bodyContent.getBytes()), "text/plain")
                .filename(null)
                .perform()) {
            assertEquals(CREATED.getStatusCode(), response.getStatusCode(), "Empty filename rejected");
        }
    }

    @Test
    public void testPatch() throws Exception {
        // Create object
        try (final FcrepoResponse response = create()) {
        }

        // Update triples with sparql update
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        try (final FcrepoResponse response = client.patch(url)
                .body(body)
                .perform()) {

            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testPatchEtagUpdated() throws Exception {
        // Create object
        final EntityTag createdEtag;
        try (final FcrepoResponse createResp = create()) {
            createdEtag = EntityTag.valueOf(createResp.getHeaderValue(ETAG));
        }

        // The weak ETag is derived from the last-modified timestamp, which has
        // second granularity, so ensure the patch lands in a later second
        Thread.sleep(1500);

        // Update triples with sparql update
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        try (final FcrepoResponse response = client.patch(url)
                .body(body)
                .ifMatch("\"" + createdEtag.getValue() + "\"")
                .perform()) {

            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        // As of Fedora 7 the PATCH response no longer carries the updated ETag,
        // so retrieve it with a fresh request
        try (final FcrepoResponse response = client.head(url).perform()) {
            final EntityTag updateEtag = EntityTag.valueOf(response.getHeaderValue(ETAG));

            assertNotEquals(createdEtag, updateEtag, "Etag did not change after patch");
        }
    }

    @Test
    public void testPatchNoBody() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        try (final FcrepoResponse response = client.patch(url)
                .perform()) {
            assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testDelete() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        try (final FcrepoResponse response = client.delete(url).perform()) {

            assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

            assertEquals(GONE.getStatusCode(), client.get(url).perform().getStatusCode());
        }
    }

    @Test
    public void testGet() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        try (final FcrepoResponse response = client.get(url).perform()) {
            assertEquals(OK.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        try (final FcrepoResponse response = client.get(url).perform()) {
            assertEquals(NOT_FOUND.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testGetUnmodified() throws Exception {

        final String lastModified;
        try (final FcrepoResponse response = create()) {
            lastModified = response.getHeaderValue(LAST_MODIFIED);
        }

        // Get tomorrows date to provide as the modified-since date
        final Date modDate = DateUtils.parseDate(lastModified);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(modDate);
        cal.add(Calendar.DATE, 1);

        // Check that get returns a 304 if the item hasn't changed according to last-modified
        try (final FcrepoResponse modResp = client.get(url)
                .ifModifiedSince(DateUtils.formatDate(cal.getTime()))
                .perform()) {

            assertEquals(NOT_MODIFIED.getStatusCode(), modResp.getStatusCode());
            assertNull(modResp.getBody(), "Response body should not be returned when unmodified");
        }
    }

    @Test
    public void testGetModified() throws Exception {

        final String lastModified;
        try (final FcrepoResponse response = create()) {
            lastModified = response.getHeaderValue(LAST_MODIFIED);
        }

        // Get yesterdays date to provide as the modified-since date
        final Date modDate = DateUtils.parseDate(lastModified);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(modDate);
        cal.add(Calendar.DATE, -1);

        // Check that get returns a 200 if the item has changed according to last-modified
        try (final FcrepoResponse modResp = client.get(url)
                .ifModifiedSince(DateUtils.formatDate(cal.getTime()))
                .perform()) {

            assertEquals(OK.getStatusCode(), modResp.getStatusCode());
            assertNotNull(modResp.getBody(), "GET response body should be normal when modified");
        }
    }

    @Test
    public void testGetAccept() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        try (final FcrepoResponse response = client.get(url)
                .accept("application/n-triples")
                .perform()) {

            assertEquals("application/n-triples", response.getHeaderValue(CONTENT_TYPE));
            assertEquals(OK.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testGetPrefer() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        try (final FcrepoResponse response = client.get(url)
                .preferRepresentation(Arrays.asList(URI.create("http://www.w3.org/ns/ldp#PreferMinimalContainer")),
                        Arrays.asList(URI.create("http://fedora.info/definitions/fcrepo#ServerManaged"))).perform()) {

            assertEquals(OK.getStatusCode(), response.getStatusCode());
            assertEquals("return=representation; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"; " +
                    "omit=\"http://fedora.info/definitions/fcrepo#ServerManaged\"",
                    response.getHeaderValue("Preference-Applied"));
        }
    }

    @Test
    public void testGetRange() throws Exception {
        // Creating a binary for retrieval
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final URI createdResourceUri;
        try (final FcrepoResponse response = client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform()) {

            createdResourceUri = response.getLocation();
        }

        // Get the content of the object after the first 6 bytes
        try (final FcrepoResponse rangeResp = client.get(createdResourceUri)
                .range(6L, null)
                .perform()) {

            final String content = IOUtils.toString(rangeResp.getBody(), "UTF-8");
            assertEquals("world", content, "Body did not contain correct range of original content");
            assertEquals(PARTIAL_CONTENT.getStatusCode(), rangeResp.getStatusCode());
        }
    }

    @Test
    public void testGetDisableRedirects() throws Exception {
        final String filename = "example.html";
        final String mimetype = "text/plain";

        final URI externalURI = URI.create("http://example.com/");
        final URI resourceLocation;

        try (final FcrepoResponse response = client.post(new URI(SERVER_ADDRESS))
                .externalContent(externalURI, mimetype, ExternalContentHandling.REDIRECT)
                .filename(filename)
                .perform()
                ) {
                    resourceLocation = response.getLocation();
        }

                try (final FcrepoResponse getResponse = client.get(resourceLocation)
                .disableRedirects()
                .perform();
                ) {
            assertEquals(TEMPORARY_REDIRECT.getStatusCode(), getResponse.getStatusCode(),
                    "Didn't get a REDIRECT response!");
            assertEquals(mimetype, getResponse.getContentType());

            final Map<String, String> contentDisp = getResponse.getContentDisposition();
            assertEquals(filename, contentDisp.get(CONTENT_DISPOSITION_FILENAME));

            assertEquals(externalURI, getResponse.getLocation());
        }
    }

    @Test
    public void testGetWantDigest() throws Exception {
        // Creating a binary for retrieval
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";

        final URI createdResourceUri;
        try (final FcrepoResponse response = client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform()) {
            createdResourceUri = response.getLocation();
        }

        // Request md5 digest with caching disabled
        try (final FcrepoResponse getResp = client.get(createdResourceUri)
                .wantDigest("md5")
                .noCache()
                .perform()) {
            assertEquals(OK.getStatusCode(), getResp.getStatusCode());

            final String digest = getResp.getHeaderValue(DIGEST);
            assertTrue(digest.contains("md5=3e25960a79dbc69b674cd4ec67a72c62"), "Did not contain md5");
        }
    }

    @Test
    public void testHead() throws Exception {
        try (final FcrepoResponse response = create();
                final FcrepoResponse headResp = client.head(url).perform()) {

            assertEquals(OK.getStatusCode(), headResp.getStatusCode());
            assertEquals(response.getHeaderValue(ETAG), headResp.getHeaderValue(ETAG));
            assertNotNull(headResp.getHeaderValue("Allow"));
        }
    }

    @Test
    public void testHeadWantDigest() throws Exception {
        // Creating a binary for retrieval
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final URI createdResourceUri;
        try (final FcrepoResponse response = client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform()) {

            createdResourceUri = response.getLocation();
        }

        final Map<String, Double> qualityMap = new HashMap<>();
        qualityMap.put("md5", 1.0);
        qualityMap.put("sha", 1.0);
        qualityMap.put("sha-256", 0.4);

        final String wantDigest = HeaderHelpers.formatQualityValues(qualityMap);
        // Request multiple digests
        try (final FcrepoResponse getResp = client.head(createdResourceUri)
                .wantDigest(wantDigest)
                .perform()) {
            assertEquals(OK.getStatusCode(), getResp.getStatusCode());

            final String digest = getResp.getHeaderValue(DIGEST);
            assertTrue(digest.contains("md5=3e25960a79dbc69b674cd4ec67a72c62"), "Did not contain md5");
            assertTrue(digest.contains("sha=7b502c3a1f48c8609ae212cdfb639dee39673f5e"), "Did not contain sha1");
            assertTrue(digest.contains("sha-256=64ec88ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c"),
                    "Did not contain sha256");
        }
    }

    @Test
    public void testOptions() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        try (final FcrepoResponse headResp = client.options(url).perform()) {

            assertEquals(OK.getStatusCode(), headResp.getStatusCode());
            assertNotNull(headResp.getHeaderValue("Allow"));
            assertNotNull(headResp.getHeaderValue("Accept-Post"));
            assertNotNull(headResp.getHeaderValue("Accept-Patch"));
        }
    }

    @Test
    public void testStateTokens() throws Exception {
        try (final FcrepoResponse response = create()) {
        }

        final String stateToken;
        try (final FcrepoResponse getResp = client.get(url).perform()) {
            stateToken = getResp.getHeaderValue(STATE_TOKEN);
        }

        // Attempt to update triples with an incorrect state token
        final InputStream body = new ByteArrayInputStream(sparqlUpdate.getBytes());
        try (final FcrepoResponse badTokenResp = client.patch(url)
                .body(body)
                .ifStateToken("bad_state_token")
                .perform()) {
            assertEquals(PRECONDITION_FAILED.getStatusCode(), badTokenResp.getStatusCode());
        }

        // Update triples with the correct state token
        body.reset();
        try (final FcrepoResponse goodTokenResp = client.patch(url)
                .body(body)
                .ifStateToken(stateToken)
                .perform()) {
            assertEquals(NO_CONTENT.getStatusCode(), goodTokenResp.getStatusCode());
        }
    }

    private FcrepoResponse create() throws FcrepoOperationFailedException {
        return client.put(url).perform();
    }

    private String getTurtle(final URI url) throws Exception {
        try (final FcrepoResponse getResponse = client.get(url)
                .accept("text/turtle")
                .preferRepresentation(Arrays.asList(URI.create("http://www.w3.org/ns/ldp#PreferMinimalContainer")),
                        Arrays.asList(URI.create("http://fedora.info/definitions/fcrepo#ServerManaged"))).perform()) {
            return IOUtils.toString(getResponse.getBody(), "UTF-8");
        }
    }
}
