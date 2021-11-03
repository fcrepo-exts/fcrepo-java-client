/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client.integration;

import static javax.ws.rs.core.Response.Status.CREATED;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.fcrepo.client.FedoraTypes.MEMENTO_TYPE;
import static org.fcrepo.client.LinkHeaderConstants.MEMENTO_TIME_MAP_REL;
import static org.fcrepo.client.TestUtils.rdfTtl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author bbpennel
 */
public class VersioningIT extends AbstractResourceIT {

    private static FcrepoClient client;

    private static final Property DC_TITLE = createProperty("http://purl.org/dc/elements/1.1/title");

    @BeforeClass
    public static void beforeClass() {
        client = FcrepoClient.client()
                .credentials("fedoraAdmin", "fedoraAdmin")
                .authScope("localhost")
                .build();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        client.close();
    }

    @Test
    public void testCreateMementoFromOriginal() throws Exception {
        final URI timemapURI;
        final URI mementoURI;

        // Create original resource with custom property
        try (final FcrepoResponse createOriginalResp = client.post(new URI(SERVER_ADDRESS))
                .body(new ByteArrayInputStream(rdfTtl.getBytes()), "text/turtle")
                .perform()) {
            timemapURI = createOriginalResp.getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);
        }

        try (final FcrepoResponse mementoResp = client.createMemento(timemapURI)
                .perform()) {
            assertEquals(CREATED.getStatusCode(), mementoResp.getStatusCode());
            mementoURI = mementoResp.getLocation();
        }

        try (final FcrepoResponse getResp = client.get(mementoURI).perform()) {
            assertTrue("Retrieved object must be a memento", getResp.hasType(MEMENTO_TYPE));
            final Model respModel = getResponseModel(getResp);
            assertTrue(respModel.contains(null, DC_TITLE, "Test Object"));
        }
    }

    @Test
    public void testDatetimeNegotiation() throws Exception {
        final URI location;
        final URI timemapURI;

        try (final FcrepoResponse createOriginalResp = client.post(new URI(SERVER_ADDRESS))
                .perform()) {
            location = createOriginalResp.getLocation();
            timemapURI = createOriginalResp.getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);
        }

        // create version
        try (final FcrepoResponse mementoResp = client.createMemento(timemapURI).perform()) {
        }

        // Negotiate for the most recent memento
        try (final FcrepoResponse negotiateResp = client.get(location)
                .acceptDatetime(Instant.now())
                .perform()) {
            assertTrue(negotiateResp.hasType(MEMENTO_TYPE));
        }
    }

}
