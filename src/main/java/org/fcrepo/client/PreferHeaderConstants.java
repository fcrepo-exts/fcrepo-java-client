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

import java.net.URI;

/**
 * Constants for Prefer headers
 *
 * @author bbpennel
 */
public class PreferHeaderConstants {

    // Embed "child" resources in the returned representation
    public final static URI PREFER_CONTAINED_DESCRIPTIONS = URI.create(
            "http://www.w3.org/ns/oa#PreferContainedDescriptions");

    // Include/Exclude "ldp:contains" assertions to contained resources
    public final static URI PREFER_CONTAINMENT = URI.create("http://www.w3.org/ns/ldp#PreferContainment");

    // Include/Exclude assertions to member resources established by the Direct and Indirect containers
    public final static URI PREFER_MEMBERSHIP = URI.create("http://www.w3.org/ns/ldp#PreferMembership");

    // Include/Exclude triples that would be present when the container is empty
    public final static URI PREFER_MINIMAL_CONTAINER = URI.create("http://www.w3.org/ns/ldp#PreferMinimalContainer");

    // Include assertions from other Fedora resources to this node
    public final static URI PREFER_INBOUND_REFERENCES = URI.create(
            "http://fedora.info/definitions/fcrepo#PreferInboundReferences");

    // Embed server managed properties in the representation
    public final static URI PREFER_SERVER_MANAGED = URI.create(
            "http://fedora.info/definitions/v4/repository#ServerManaged");

    // Allows replacing the properties of a container without having to provide all of the server-managed triples
    public final static String HANDLING_LENIENT = "handling=lenient; received=\"minimal\"";

    // links to other resources and their properties should be included
    public final static String RETURN_REPRESENTATION = "return=representation";

    // only triples directly related to a resource should be returned
    public final static String RETURN_MINIMAL = "return=minimal";

    private PreferHeaderConstants() {
    }
}
