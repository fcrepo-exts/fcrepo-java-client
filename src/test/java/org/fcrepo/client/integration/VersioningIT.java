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

import static javax.ws.rs.core.Response.Status.CREATED;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.fcrepo.client.FedoraHeaderConstants.MEMENTO_DATETIME;
import static org.fcrepo.client.FedoraTypes.MEMENTO_TYPE;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;
import static org.fcrepo.client.LinkHeaderConstants.DESCRIBEDBY_REL;
import static org.fcrepo.client.LinkHeaderConstants.MEMENTO_TIME_MAP_REL;
import static org.fcrepo.client.TestUtils.rdfTtl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;
import org.fcrepo.kernel.api.RdfLexicon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author bbpennel
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-test/test-container.xml")
public class VersioningIT extends AbstractResourceIT {

    private static final Property DC_TITLE = createProperty("http://purl.org/dc/elements/1.1/title");

    private final ZonedDateTime HISTORIC_DATETIME = LocalDateTime.of(2000, 1, 1, 0, 0).atZone(ZoneOffset.UTC);

    private final String HISTORIC_TIMESTAMP = UTC_RFC_1123_FORMATTER.format(HISTORIC_DATETIME);

    public VersioningIT() {
        super();

        client = FcrepoClient.client()
                .credentials("fedoraAdmin", "password")
                .authScope("localhost")
                .build();
    }

    @Test
    public void testCreateMementoFromOriginal() throws Exception {
        // Create original resource with custom property
        final FcrepoResponse createOriginalResp = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(rdfTtl.getBytes()), "text/turtle")
                .perform();

        final URI timemapURI = createOriginalResp.getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);

        final FcrepoResponse mementoResp = client.createMemento(timemapURI)
                .perform();
        assertEquals(CREATED.getStatusCode(), mementoResp.getStatusCode());

        final URI mementoURI = mementoResp.getLocation();

        final FcrepoResponse getResp = client.get(mementoURI).perform();
        assertTrue("Retrieved object must be a memento", getResp.hasType(MEMENTO_TYPE));
        final Model respModel = getResponseModel(getResp);
        assertTrue(respModel.contains(null, DC_TITLE, "Test Object"));
    }

    // create memento from historic version, binary
    @Test
    public void testCreateHistoricBinaryMemento() throws Exception {
        // Create original binary
        final String mimetype = "text/plain";
        final String bodyContent = "Hello world";
        final FcrepoResponse createOriginalResp = client.post(new URI(serverAddress))
                .body(new ByteArrayInputStream(bodyContent.getBytes()), mimetype)
                .perform();

        final URI originalURI = createOriginalResp.getLocation();
        final URI timemapURI = createOriginalResp.getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);

        // Create memento of the binary
        final String mementoType = "text/old";
        final String mementoContent = "Hello old world";
        final FcrepoResponse binMementoResp = client.createMemento(timemapURI, HISTORIC_TIMESTAMP)
                .body(new ByteArrayInputStream(mementoContent.getBytes()), mementoType)
                .perform();
        assertEquals(CREATED.getStatusCode(), binMementoResp.getStatusCode());

        // Determine location of description and its timemap
        final URI descURI = binMementoResp.getLinkHeaders(DESCRIBEDBY_REL).get(0);
        final URI descTimeMapURI = client.head(descURI).perform().getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);

        // Create memento of the binary description
        final String titleValue = "Ancient Binary";
        // Modify the original model before creating memento
        final Model originalModel = getResponseModel(client.get(descURI).perform());
        final Resource resc = originalModel.getResource(originalURI.toString());
        resc.addLiteral(DC_TITLE, titleValue);
        // Set the updated mimetype for the memento
        resc.removeAll(RdfLexicon.HAS_MIME_TYPE);
        resc.addLiteral(RdfLexicon.HAS_MIME_TYPE, mementoType);

        final FcrepoResponse descMementoResp = client.createMemento(descTimeMapURI, HISTORIC_TIMESTAMP)
                .body(modelToInputStream(originalModel), "text/turtle")
                .perform();
        assertEquals(CREATED.getStatusCode(), descMementoResp.getStatusCode());

        // Verify the historic binary was created
        final URI binaryMementoURI = binMementoResp.getLocation();
        final FcrepoResponse getBinMementoResp = client.get(binaryMementoURI).perform();
        final String getBinContent = IOUtils.toString(getBinMementoResp.getBody(), "UTF-8");

        assertEquals(mementoContent, getBinContent);
        assertEquals(mementoType, getBinMementoResp.getContentType());
        assertTrue("Retrieved object must be a memento", getBinMementoResp.hasType(MEMENTO_TYPE));
        assertEquals("Memento did not have expected datetime",
                HISTORIC_TIMESTAMP, getBinMementoResp.getHeaderValue(MEMENTO_DATETIME));

        // Find the memento of the description by its memento datetime
        final FcrepoResponse mementoDescNegoResp = client.get(descURI)
                .acceptDatetime(HISTORIC_TIMESTAMP)
                .perform();

        assertEquals("Memento did not have expected datetime",
                HISTORIC_TIMESTAMP, mementoDescNegoResp.getHeaderValue(MEMENTO_DATETIME));
        final Model respModel = getResponseModel(mementoDescNegoResp);
        assertTrue("Memento must contain provided property",
                respModel.contains(createResource(originalURI.toString()), DC_TITLE, titleValue));
    }

    // create memento from historic version, container
    @Test
    public void testCreateHistoricContainerMemento() throws Exception {
        // Create original resource with custom property
        final FcrepoResponse createOriginalResp = client.post(new URI(serverAddress))
                .perform();

        final URI originalURI = createOriginalResp.getLocation();
        final URI timemapURI = createOriginalResp.getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);

        // Add a property to the resource to use as the historic version
        final String titleValue = "Very Historical";
        final FcrepoResponse originalResp = client.get(originalURI).perform();
        final Model originalModel = getResponseModel(originalResp);
        originalModel.getResource(originalURI.toString())
                .addLiteral(DC_TITLE, titleValue);

        // Create historic memento with updated model
        final FcrepoResponse mementoResp = client.createMemento(timemapURI, HISTORIC_DATETIME.toInstant())
                .body(modelToInputStream(originalModel), "text/turtle")
                .perform();
        assertEquals(CREATED.getStatusCode(), mementoResp.getStatusCode());

        // Verify that the memento matches expectations
        final URI mementoURI = mementoResp.getLocation();
        final FcrepoResponse getResp = client.get(mementoURI).perform();
        assertTrue("Retrieved object must be a memento", getResp.hasType(MEMENTO_TYPE));
        assertEquals("Memento did not have expected datetime",
                HISTORIC_TIMESTAMP, getResp.getHeaderValue(MEMENTO_DATETIME));
        final Model respModel = getResponseModel(getResp);
        assertTrue("Memento must contain provided property",
                respModel.contains(null, DC_TITLE, titleValue));
    }

    @Test
    public void testDatetimeNegotiation() throws Exception {
        final FcrepoResponse createOriginalResp = client.post(new URI(serverAddress))
                .perform();

        final URI location = createOriginalResp.getLocation();
        final URI timemapURI = createOriginalResp.getLinkHeaders(MEMENTO_TIME_MAP_REL).get(0);
        // create version
        client.createMemento(timemapURI).perform();

        // Negotiate for the most recent memento
        final FcrepoResponse negotiateResp = client.get(location)
                .acceptDatetime(Instant.now())
                .perform();

        assertTrue(negotiateResp.hasType(MEMENTO_TYPE));
    }

    private InputStream modelToInputStream(final Model model) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "text/turtle");
        return new ByteArrayInputStream(out.toByteArray());
    }
}
