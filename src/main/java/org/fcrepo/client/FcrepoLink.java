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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A class representing the value of an HTTP Link header
 *
 * @author Aaron Coburn
 * @author bbpennel
 */
public class FcrepoLink {

    private static final String PARAM_DELIM = ";";

    private static final String META_REL = "rel";

    private static final String META_TYPE = "type";

    private URI uri;

    private Map<String, String> params;

    /**
     * Create a representation of a Link header.
     *
     * @param link the value for a Link header
     */
    public FcrepoLink(final String link) {
        if (link == null) {
            throw new IllegalArgumentException("Link header did not contain a URI");
        }
        this.params = new HashMap<>();
        parse(link);
    }

    /**
     * Construct a representation of a Link header from the given uri and parameters.
     *
     * @param uri URI portion of the link header
     * @param params link parameters
     */
    private FcrepoLink(final URI uri, final Map<String, String> params) {
        this.uri = uri;
        this.params = params;
    }

    /**
     * Retrieve the URI of the link
     *
     * @return the URI portion of a Link header
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Retrieve the REL portion of the link
     *
     * @return the "rel" portion of a Link header
     */
    public String getRel() {
        return getParam(META_REL);
    }

    /**
     * Retrieve the type portion of the link
     *
     * @return the "type" parameter of the header
     */
    public String getType() {
        return getParam(META_TYPE);
    }

    /**
     * Retrieve a parameter from the link header
     *
     * @param name name of the parameter in the link header
     * @return the value of the parameter or null if not present.
     */
    public String getParam(final String name) {
        return params.get(name);
    }

    /**
     * Retrieve a map of parameters from the link header
     *
     * @return map of parameters
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Parse the value of a link header
     */
    private void parse(final String link) {
        final int paramIndex = link.indexOf(PARAM_DELIM);
        if (paramIndex == -1) {
            uri = getLinkPart(link);
        } else {
            uri = getLinkPart(link.substring(0, paramIndex));
            // Parse the remainder of the header after the URI as parameters
            parseParams(link.substring(paramIndex + 1));
        }
    }

    private void parseParams(final String paramString) {
        final StringTokenizer st = new StringTokenizer(paramString, ";\",", true);
        while (st.hasMoreTokens()) {
            // Read one parameter, until an unquoted ; is encountered or no more tokens
            boolean inQuotes = false;
            final StringBuilder paramBuilder = new StringBuilder();
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                if (token.equals("\"")) {
                    inQuotes = !inQuotes;
                } else if (!inQuotes && token.equals(";")) {
                    break;
                } else if (!inQuotes && token.equals(",")) {
                    throw new IllegalArgumentException("Cannot parse link, contains unterminated quotes");
                } else {
                    paramBuilder.append(token);
                }
            }

            if (inQuotes) {
                throw new IllegalArgumentException("Cannot parse link, contains unterminated quotes");
            }

            final String param = paramBuilder.toString();
            final String[] components = param.split("=", 2);
            if (components.length == 2) {
                final String name = components[0].trim();
                final String value = components[1].trim();
                params.put(name, value);
            } else {
                throw new IllegalArgumentException(
                        "Cannot parse link, improperly structured parameter encountered: " + param);
            }
        }
    }

    private static String stripQuotes(final String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append('<').append(uri.toString()).append('>');

        params.forEach((name, value) -> {
            result.append("; ").append(name).append("=\"").append(value).append('"');
        });
        return result.toString();
    }

    /**
     * Extract the URI part of the link header
     */
    private static URI getLinkPart(final String uriPart) {
        final String linkPart = uriPart.trim();
        if (!linkPart.startsWith("<") || !linkPart.endsWith(">")) {
            throw new IllegalArgumentException("Link header did not contain a URI");
        } else {
            return URI.create(linkPart.substring(1, linkPart.length() - 1));
        }
    }

    /**
     * Create a new builder instance initialized from an existing URI represented as a string.
     *
     * @param uri URI which will be used to initialize the builder
     * @return a new link builder.
     * @throws IllegalArgumentException if uri is {@code null}.
     */
    public static Builder fromUri(final String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is required");
        }
        return new Builder().uri(uri);
    }

    /**
     * Create a new builder instance initialized from an existing URI.
     *
     * @param uri URI which will be used to initialize the builder
     * @return a new link builder.
     * @throws IllegalArgumentException if uri is {@code null}.
     */
    public static Builder fromUri(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is required");
        }
        return new Builder().uri(uri);
    }

    /**
     * Simple parser to convert a link header containing a single link into an FcrepoLink object.
     *
     * @param link link header value.
     * @return FcrepoLink representing the link
     */
    public static FcrepoLink valueOf(final String link) {
        return new FcrepoLink(link);
    }

    /**
     * Parser which converts a link header containing one or more links into a list of FcrepoLink objects.
     *
     * @param headerValue link header value.
     * @return List containing an FcrepoLink for each link in the header value.
     */
    public static List<FcrepoLink> fromHeader(final String headerValue) {
        final List<FcrepoLink> links = new ArrayList<>();

        // States which indicate that a "," is not currently a link delimiter
        boolean inQuotes = false;
        boolean inUri = false;

        // Split the header into separate links and create FcrepoLink objects for each
        // Allows for reserved characters to appear within quoted values or the URI
        StringBuilder currentLink = new StringBuilder();
        final StringTokenizer st = new StringTokenizer(headerValue, ",\"<>", true);
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (token.equals(",")) {
                // Link delimiter, add current link to result and start next link
                if (!inQuotes && !inUri) {
                    links.add(new FcrepoLink(currentLink.toString().trim()));
                    currentLink = new StringBuilder();
                    continue;
                }
            } else if (token.equals("\"") && !inUri) {
                inQuotes = !inQuotes;
            } else if (token.equals("<") && !inQuotes) {
                inUri = true;
            } else if (token.equals(">") && !inQuotes) {
                inUri = false;
            }

            // Accumulate tokens composing this link
            currentLink.append(token);
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Cannot parse link header, contains unterminated quotes: "
                    + headerValue);
        }
        if (inUri) {
            throw new IllegalArgumentException("Cannot parse link header, contains unterminated URI: "
                    + headerValue);
        }

        links.add(new FcrepoLink(currentLink.toString().trim()));
        return links;
    }

    /**
     * Builder class for link headers represented as FcrepoLinks
     *
     * @author bbpennel
     */
    public static class Builder {

        private URI uri;

        private Map<String, String> params;

        /**
         * Construct a builder
         */
        public Builder() {
            this.params = new HashMap<>();
        }

        /**
         * Set the URI for this link
         *
         * @param uri URI for link
         * @return this builder
         */
        public Builder uri(final URI uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Set the URI for this link
         *
         * @param uri URI for link
         * @return this builder
         */
        public Builder uri(final String uri) {
            this.uri = URI.create(uri);
            return this;
        }

        /**
         * Set a rel parameter for this link
         *
         * @param rel rel param value
         * @return this builder
         */
        public Builder rel(final String rel) {
            return param(META_REL, rel);
        }

        /**
         * Set a type parameter for this link
         *
         * @param type type param value
         * @return this builder
         */
        public Builder type(final String type) {
            return param(META_TYPE, type);
        }

        /**
         * Set an arbitrary parameter for this link
         *
         * @param name name of the parameter
         * @param value value of the parameter
         * @return this builder
         */
        public Builder param(final String name, final String value) {
            params.put(name, stripQuotes(value));
            return this;
        }

        /**
         * Finish building this link.
         *
         * @return newly built link.
         */
        public FcrepoLink build() {
            return new FcrepoLink(uri, params);
        }
    }
}
