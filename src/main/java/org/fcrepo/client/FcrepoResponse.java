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

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_TYPE;
import static org.fcrepo.client.FedoraHeaderConstants.DESCRIBED_BY;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;
import static org.fcrepo.client.FedoraHeaderConstants.LOCATION;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;

/**
 * Represents a response from a fedora repository using a {@link FcrepoClient}.
 * <p>
 * This class implements {@link Closeable}. Suggested usage is to create the {@code FcrepoResponse} within a
 * try-with-resources block, insuring that any resources held by the response are freed automatically.
 * </p>
 * <pre>
 * FcrepoClient client = ...;
 * try (FcrepoResponse res = client.get(...)) {
 *     // do something with the response
 * } catch (FcrepoOperationFailedException|IOException e) {
 *     // handle any exceptions
 * }
 * </pre> Closed responses have no obligation to provide access to released resources.
 *
 * @author Aaron Coburn
 * @since October 20, 2014
 */
public class FcrepoResponse implements Closeable {

    private URI url;

    private int statusCode;

    private URI location;

    private Map<String, List<String>> headers;

    private Map<String, String> contentDisposition;

    private InputStream body;

    private String contentType;

    private boolean closed = false;

    /**
     * Create a FcrepoResponse object from the http response
     *
     * @param url the requested URL
     * @param statusCode the HTTP status code
     * @param headers a map of all response header names and values
     * @param body the response body stream
     */
    public FcrepoResponse(final URI url, final int statusCode, final Map<String, List<String>> headers,
            final InputStream body) {
        this.setUrl(url);
        this.setStatusCode(statusCode);
        this.setHeaders(headers);
        this.setBody(body);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Invoking this method will close the underlying {@code InputStream} containing the entity
     * body of the HTTP response.
     * </p>
     *
     * @throws IOException if there is an error closing the underlying HTTP response stream.
     */
    @Override
    public void close() throws IOException {
        if (!this.closed && this.body != null) {
            try {
                this.body.close();
            } finally {
                this.closed = true;
            }
        }
    }

    /**
     * Whether or not the resources have been freed from this response. There should be no expectation that a closed
     * response provides access to the {@link #getBody() entity body}.
     *
     * @return {@code true} if resources have been freed, otherwise {@code false}
     */
    public boolean isClosed() {
        return closed;
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
     * headers getter
     * 
     * @return headers from the response
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Get all values for the specified header
     * 
     * @param name name of the header to retrieve
     * @return All values of the specified header, or null if not present
     */
    public List<String> getHeaderValues(final String name) {
        if (headers == null) {
            return null;
        }

        return headers.get(name);
    }

    /**
     * Get the first value for the specified header
     * 
     * @param name name of the header to retrieve
     * @return First value of the header, or null if not present
     */
    public String getHeaderValue(final String name) {
        final List<String> values = getHeaderValues(name);
        if (values == null || values.size() == 0) {
            return null;
        }

        return values.get(0);
    }

    /**
     * headers setter
     * 
     * @param headers headers from the response
     */
    public void setHeaders(final Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Retrieve link header values matching the given relationship
     * 
     * @param relationship the relationship of links to return
     * @return list of link header URIs matching the given relationship
     */
    public List<URI> getLinkHeaders(final String relationship) {
        final List<URI> uris = new ArrayList<URI>();
        final List<String> linkStrings = getHeaderValues(LINK);
        if (linkStrings == null) {
            return null;
        }

        for (String linkString : linkStrings) {
            final FcrepoLink link = new FcrepoLink(linkString);
            if (link.getRel().equals(relationship)) {
                uris.add(link.getUri());
            }
        }

        return uris;
    }

    /**
     * location getter
     * 
     * @return the location of a related resource
     */
    public URI getLocation() {
        if (location == null && headers != null) {
            // Retrieve the value from the location header if available
            final String value = getHeaderValue(LOCATION);
            if (value != null) {
                location = URI.create(getHeaderValue(LOCATION));
            }
            // Fall back to retrieving from the described by link
            if (location == null) {
                final List<URI> links = getLinkHeaders(DESCRIBED_BY);
                if (links != null && links.size() == 1) {
                    location = links.get(0);
                }
            }
        }

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
        if (contentType == null && headers != null) {
            contentType = getHeaderValue(CONTENT_TYPE);
        }
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

    /**
     * Get a map of parameters from the Content-Disposition header if present
     * 
     * @return map of Content-Disposition parameters or null
     */
    public Map<String, String> getContentDisposition() {
        if (contentDisposition == null && headers.containsKey(CONTENT_DISPOSITION)) {
            List<String> values = headers.get(CONTENT_DISPOSITION);
            if (values.isEmpty()) {
                return null;
            }

            contentDisposition = new HashMap<>();
            String value = values.get(0);
            BasicHeader header = new BasicHeader(CONTENT_DISPOSITION, value);
            for (HeaderElement headEl : header.getElements()) {
                for (NameValuePair pair : headEl.getParameters()) {
                    contentDisposition.put(pair.getName(), pair.getValue());
                }
            }
        }
        return contentDisposition;
    }
}
