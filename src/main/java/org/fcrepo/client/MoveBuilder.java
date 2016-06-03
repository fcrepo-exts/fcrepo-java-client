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
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

/**
 * Builds a request to move a resource (and its subtree) to a new location
 * 
 * @author bbpennel
 */
public class MoveBuilder<T extends MoveBuilder<T>> extends RequestBuilder<MoveBuilder<T>> {

    private static final Logger LOGGER = getLogger(MoveBuilder.class);

    protected URI destinationUrl;

    protected HttpMethods method;

    protected MoveBuilder(URI sourceUrl, URI destinationUrl, FcrepoClient client) {
        super(sourceUrl, client);
        this.destinationUrl = destinationUrl;
        method = HttpMethods.MOVE;
    }

    @Override
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        final HttpRequestBase request = method.createRequest(targetUri);

        if (destinationUrl != null) {
            request.addHeader(DESTINATION, destinationUrl.toString());
        }

        LOGGER.debug("Fcrepo {} request of {} to {}", method.name(), targetUri, destinationUrl);

        return client.executeRequest(targetUri, request);
    }

    @Override
    protected MoveBuilder<T> self() {
        return this;
    }
}
