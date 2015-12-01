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

/**
 * A class representing the value of an HTTP Link header
 *
 * @author Aaron Coburn
 */
public class FcrepoLink {

    private static final String LINK_DELIM = ";";

    private static final String META_REL = "rel";

    private URI uri;

    private String rel;

    /**
     * Create a representation of a Link header.
     *
     * @param link the value for a Link header
     */
    public FcrepoLink(final String link) {
        parse(link);
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
        return rel;
    }

    /**
     * Parse the value of a link header
     */
    private void parse(final String link) {
        if (link != null) {
            final String[] segments = link.split(LINK_DELIM);
            if (segments.length == 2) {
                uri = getLinkPart(segments[0]);
                if (uri != null) {
                    rel = getRelPart(segments[1]);
                }
            }
        }
    }

    /**
     * Extract the rel="..." part of the link header
     */
    private static String getRelPart(final String relPart) {
        final String[] segments = relPart.trim().split("=");
        if (segments.length != 2 || !META_REL.equals(segments[0])) {
            return null;
        }
        final String relValue = segments[1];
        if (relValue.startsWith("\"") && relValue.endsWith("\"")) {
            return relValue.substring(1, relValue.length() - 1);
        } else {
            return relValue;
        }
    }

    /**
     * Extract the URI part of the link header
     */
    private static URI getLinkPart(final String uriPart) {
        final String linkPart = uriPart.trim();
        if (!linkPart.startsWith("<") || !linkPart.endsWith(">")) {
            return null;
        } else {
            return URI.create(linkPart.substring(1, linkPart.length() - 1));
        }
    }
}
