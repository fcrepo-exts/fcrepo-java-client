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

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a request to delete a resource
 * 
 * @author bbpennel
 */
public class DeleteBuilder<T extends DeleteBuilder<T>> extends RequestBuilder<DeleteBuilder<T>> {

    /**
     * Instantiate builder
     * 
     * @param uri uri request will be issued to
     * @param client the client
     */
    public DeleteBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.DELETE.createRequest(targetUri);
    }

    @Override
    protected DeleteBuilder<T> self() {
        return this;
    }
}
