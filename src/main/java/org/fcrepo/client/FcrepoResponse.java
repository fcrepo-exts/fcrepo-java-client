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

import java.io.InputStream;
import java.net.URI;

/**
 * Represents a response from a fedora repository using a {@link FcrepoClient}.
 *
 * @author Aaron Coburn
 * @since October 20, 2014
 */
public class FcrepoResponse {

    private URI url;

    private int statusCode;

    private URI location;

    private InputStream body;

    private String contentType;

    /**
     * Create a FcrepoResponse object from the http response
     *
     * @param url the requested URL
     * @param statusCode the HTTP status code
     * @param contentType the mime-type of the response
     * @param location the location of a related resource
     * @param body the response body stream
     */
    public FcrepoResponse(final URI url, final int statusCode,
            final String contentType, final URI location, final InputStream body) {
        this.setUrl(url);
        this.setStatusCode(statusCode);
        this.setLocation(location);
        this.setContentType(contentType);
        this.setBody(body);
    }

    /**
     * url getter
     *
     * @return the requested URL
     */
    public URI getUrl() {
        return url;
    }

    /**
     * url setter
     * 
     * @param url the requested URL
     */
    public void setUrl(final URI url) {
        this.url = url;
    }

    /**
     * statusCode getter
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * statusCode setter
     * 
     * @param statusCode the HTTP status code
     */
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * body getter
     *
     * @return the response body as a stream
     */
    public InputStream getBody() {
        return body;
    }

    /**
     * body setter
     * 
     * @param body the contents of the response body
     */
    public void setBody(final InputStream body) {
        this.body = body;
    }

    /**
     * location getter
     * 
     * @return the location of a related resource
     */
    public URI getLocation() {
        return location;
    }

    /**
     * location setter
     * 
     * @param location the value of a related resource
     */
    public void setLocation(final URI location) {
        this.location = location;
    }

    /**
     * contentType getter
     *
     * @return the mime-type of response
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * contentType setter
     *
     * @param contentType the mime-type of the response
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
}
