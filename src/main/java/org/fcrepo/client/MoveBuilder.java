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

import static org.fcrepo.client.FedoraHeaderConstants.DESTINATION;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a request to move a resource (and its subtree) to a new location
 * 
 * @author bbpennel
 */
public class MoveBuilder<T extends MoveBuilder<T>> extends RequestBuilder<MoveBuilder<T>> {

    protected URI destinationUrl;

    /**
     * Instantiate builder
     * 
     * @param sourceUrl uri of the resource
     * @param destinationUrl uri for the new path for the moved resource
     * @param client the client
     */
    protected MoveBuilder(final URI sourceUrl, final URI destinationUrl, final FcrepoClient client) {
        super(sourceUrl, client);
        this.destinationUrl = destinationUrl;
    }

    @Override
    protected MoveBuilder<T> self() {
        return this;
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.MOVE.createRequest(targetUri);
    }

    @Override
    protected void populateRequest(final HttpRequestBase request) {
        if (destinationUrl != null) {
            request.addHeader(DESTINATION, destinationUrl.toString());
        }
    }
}
