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

/**
 * Link header values used in the Fedora specification.
 *
 * @author bbpennel
 */
public class LinkHeaderConstants {

    // handling parameter for external content Links
    public static final String EXTERNAL_CONTENT_HANDLING = "handling";

    // rel value for external content URI for binaries
    public static final String EXTERNAL_CONTENT_REL = "http://fedora.info/definitions/fcrepo#ExternalContent";

    // rel for identifying the ldpcv for a ldprv
    public static final String MEMENTO_TIME_MAP_REL = "timemap";

    // rel for identifying the timegate of an ldprv
    public static final String MEMENTO_TIME_GATE_REL = "timegate";

    // rel for identifying an ldprv
    public static final String MEMENTO_ORIGINAL_REL = "original";

    // rel for link header representing the type or interaction model of the object
    public static final String TYPE_REL = "type";

    // rel identifying the RDF resource describing this resource
    public static final String DESCRIBEDBY_REL = "describedby";

    private LinkHeaderConstants() {
    }
}
