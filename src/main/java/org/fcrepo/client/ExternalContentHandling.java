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
 * Constants for external content handling defined by the Fedora specification,
 * used to determine how to process the external content URI.
 *
 * @author bbpennel
 *
 */
public class ExternalContentHandling {

    /**
     * Requests that the server dereference the external content URI and treat that as if
     * it were the entity body of the request.
     */
    public static final String COPY = "copy";

    /**
     * Requests that the server record the location of the external content and handle
     * requests for that content using HTTP redirect responses with the Content-Location
     * header specifying the external content location
     */
    public static final String REDIRECT = "redirect";

    /**
     * Requests that the server record the location of the external content and handle
     * requests for that content by proxying.
     */
    public static final String PROXY = "proxy";

    private ExternalContentHandling() {
    }
}
