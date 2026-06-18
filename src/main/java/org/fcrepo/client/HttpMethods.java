/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

/**
 * Represents an HTTP method to pass to the underlying client
 *
 * @author Aaron Coburn
 * @since January 8, 2015
 */
public enum HttpMethods {

    GET(HttpGet.class, false),
    PATCH(HttpPatch.class, true),
    POST(HttpPost.class, true),
    PUT(HttpPut.class, true),
    DELETE(HttpDelete.class, false),
    HEAD(HttpHead.class, false),
    OPTIONS(HttpOptions.class, false),
    MOVE(HttpMove.class, false),
    COPY(HttpCopy.class, false);

    final Class<? extends HttpUriRequestBase> clazz;

    // whether requests of this method may carry a request body/entity
    final boolean entity;

    HttpMethods(final Class<? extends HttpUriRequestBase> clazz, final boolean entity) {
        this.clazz = clazz;
        this.entity = entity;
    }

    /**
     * Instantiate a new HttpRequst object from the method type
     *
     * @param url the URI that is part of the request
     * @return an instance of the corresponding request class
     */
    public HttpUriRequestBase createRequest(final URI url) {
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
    public static class HttpMove extends HttpUriRequestBase {

        public final static String METHOD_NAME = "MOVE";

        /**
         * Instantiate MOVE request base
         *
         * @param uri uri for the request
         */
        public HttpMove(final URI uri) {
            super(METHOD_NAME, uri);
        }
    }

    /**
     * HTTP COPY method.
     * 
     * @author bbpennel
     */
    public static class HttpCopy extends HttpUriRequestBase {

        public final static String METHOD_NAME = "COPY";

        /**
         * Instantiate COPY request base
         *
         * @param uri uri for the request
         */
        public HttpCopy(final URI uri) {
            super(METHOD_NAME, uri);
        }
    }
}
