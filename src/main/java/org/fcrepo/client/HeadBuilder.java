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

import static org.fcrepo.client.FedoraHeaderConstants.CACHE_CONTROL;
import static org.fcrepo.client.FedoraHeaderConstants.WANT_DIGEST;

import java.net.URI;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a HEAD request to retrieve resource headers.
 *
 * @author bbpennel
 */
public class HeadBuilder extends
        RequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri request will be issued to
     * @param client the client
     */
    public HeadBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
        this.request = HttpMethods.HEAD.createRequest(targetUri);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.HEAD.createRequest(targetUri);
    }

    /**
     * Disable following redirects.
     *
     * @return this builder
     */
    public HeadBuilder disableRedirects() {
        request.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
        return this;
    }

    /**
     * Provide a Want-Digest header for this request
     *
     * @param value header value, following the syntax defined in:
     *      https://tools.ietf.org/html/rfc3230#section-4.3.1
     * @return this builder
     */
    public HeadBuilder wantDigest(final String value) {
        if (value != null) {
            request.setHeader(WANT_DIGEST, value);
        }
        return this;
    }

    /**
     * Provide a Cache-Control header with value "no-cache"
     *
     * @return this builder
     */
    public HeadBuilder noCache() {
        request.setHeader(CACHE_CONTROL, "no-cache");
        return this;
    }
}
