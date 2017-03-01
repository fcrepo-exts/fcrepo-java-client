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

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a request to copy a resource (and its subtree) to a new location
 * 
 * @author bbpennel
 */
public class CopyBuilder extends MoveBuilder {

    /**
     * Instantiate builder
     * 
     * @param sourceUrl uri of the resource
     * @param destinationUrl uri for the new path for the moved resource
     * @param client the client
     */
    protected CopyBuilder(final URI sourceUrl, final URI destinationUrl, final FcrepoClient client) {
        super(sourceUrl, destinationUrl, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.COPY.createRequest(targetUri);
    }
}
