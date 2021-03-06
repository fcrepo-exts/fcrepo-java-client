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
 * Builds a POST request for creating a memento (LDPRm) from the current state of an LDPRv.
 *
 * @author bbpennel
 */
public class OriginalMementoBuilder extends RequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public OriginalMementoBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.POST.createRequest(targetUri);
    }

    @Override
    public OriginalMementoBuilder addHeader(final String name, final String value) {
        return (OriginalMementoBuilder) super.addHeader(name, value);
    }

    @Override
    public OriginalMementoBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (OriginalMementoBuilder) super.addLinkHeader(linkHeader);
    }
}
