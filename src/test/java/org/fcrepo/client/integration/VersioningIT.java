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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static javax.ws.rs.core.Response.Status.CREATED;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.fcrepo.client.FedoraTypes.MEMENTO_TYPE;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;
import static org.fcrepo.client.LinkHeaderConstants.MEMENTO_TIME_MAP_REL;
import static org.fcrepo.client.TestUtils.rdfTtl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author bbpennel
 */
public class VersioningIT extends AbstractResourceIT {

    private static final Property DC_TITLE = createProperty("http://purl.org/dc/elements/1.1/title");

    private final ZonedDateTime HISTORIC_DATETIME = LocalDateTime.of(2000, 1, 1, 0, 0).atZone(ZoneOffset.UTC);

    private final String HISTORIC_TIMESTAMP = UTC_RFC_1123_FORMATTER.format(HISTORIC_DATETIME);

    public VersioningIT() {
        super();

        client = FcrepoClient.client()
                .credentials("fedoraAdmin", "fedoraAdmin")
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
