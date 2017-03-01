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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Represents an HTTP method to pass to the underlying client
 *
 * @author Aaron Coburn
 * @since January 8, 2015
 */
public enum HttpMethods {

    GET(HttpGet.class),
    PATCH(HttpPatch.class),
    POST(HttpPost.class),
    PUT(HttpPut.class),
    DELETE(HttpDelete.class),
    HEAD(HttpHead.class),
    OPTIONS(HttpOptions.class),
    MOVE(HttpMove.class),
    COPY(HttpCopy.class);

    final Class<? extends HttpRequestBase> clazz;

    final boolean entity;

    HttpMethods(final Class<? extends HttpRequestBase> clazz) {
        this.clazz = clazz;
        entity = HttpEntityEnclosingRequestBase.class.isAssignableFrom(clazz);
    }

    /**
     * Instantiate a new HttpRequst object from the method type
     *
     * @param url the URI that is part of the request
     * @return an instance of the corresponding request class
     */
    public HttpRequestBase createRequest(final URI url) {
        try {
            return clazz.getDeclaredConstructor(URI.class).newInstance(url);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * HTTP MOVE method.
     * 
     * @author bbpennel
     */
    public static class HttpMove extends HttpRequestBase {

        public final static String METHOD_NAME = "MOVE";

        /**
         * Instantiate MOVE request base
         * 
         * @param uri uri for the request
         */
        public HttpMove(final URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }

    /**
     * HTTP COPY method.
     * 
     * @author bbpennel
     */
    public static class HttpCopy extends HttpRequestBase {

        public final static String METHOD_NAME = "COPY";

        /**
         * Instantiate COPY request base
         * 
         * @param uri uri for the request
         */
        public HttpCopy(final URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
