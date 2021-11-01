/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

/**
 * Represents a failure of the underlying HTTP client's interaction with fedora.
 *
 * @author Aaron Coburn
 * @since January 8, 2015
 */
public class FcrepoOperationFailedException extends Exception {

    private final URI url;
    private final int statusCode;
    private final String statusText;

    /**
     * Create an FcrepoOperationFailedException
     *
     * @param url the requested url
     * @param statusCode the HTTP response code
     * @param statusText the response message
     */
    public FcrepoOperationFailedException(final URI url, final int statusCode, final String statusText) {
        super("HTTP operation failed invoking " + (url != null ? url.toString() : "[null]") +
                " with statusCode: " + statusCode + " and message: " + statusText);
        this.url = url;
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    /**
     * Return the requested url
     *
     * @return the requested URL
     */
    public URI getUrl() {
        return url;
    }

    /**
     * Get the status code
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the status text
     *
     * @return the status text for the error
     */
    public String getStatusText() {
        return statusText;
    }
}
