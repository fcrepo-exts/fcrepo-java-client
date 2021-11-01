/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
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
