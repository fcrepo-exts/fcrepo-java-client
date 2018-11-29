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

import static org.fcrepo.client.FedoraHeaderConstants.DESTINATION;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.Args;

/**
 * Builds a request to move a resource (and its subtree) to a new location
 *
 * @author bbpennel
 * @deprecated the MOVE method is not supported by the Fedora 1.0 specification
 */
@Deprecated
public class MoveBuilder extends RequestBuilder {

    /**
     * Instantiate builder
     *
     * @param sourceUrl uri of the resource
     * @param destinationUrl uri for the new path for the moved resource
     * @param client the client
     */
    public MoveBuilder(final URI sourceUrl, final URI destinationUrl, final FcrepoClient client) {
        super(sourceUrl, client);
        Args.notNull(destinationUrl, "Destination URL");
        // Add the required destination header to the request
        request.addHeader(DESTINATION, destinationUrl.toString());
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.MOVE.createRequest(targetUri);
    }

    @Override
    public MoveBuilder addHeader(final String name, final String value) {
        return (MoveBuilder) super.addHeader(name, value);
    }
}
