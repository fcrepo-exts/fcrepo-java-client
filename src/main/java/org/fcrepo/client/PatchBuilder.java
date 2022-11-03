/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Builds a PUT request for interacting with the Fedora HTTP API in order to modify the triples associated with a
 * resource with SPARQL-Update.
 *
 * @author bbpennel
 */
public class PatchBuilder extends BodyRequestBuilder {

    private static final String SPARQL_UPDATE = "application/sparql-update";

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public PatchBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.PATCH.createRequest(targetUri);
    }

    /**
     * Add a body to this request from a stream, with application/sparql-update as its content type
     *
     * @param stream InputStream of the content to be sent to the server
     * @return this builder
     */
    @Override
    public PatchBuilder body(final InputStream stream) {
        return (PatchBuilder) super.body(stream, SPARQL_UPDATE);
    }

    @Override
    public PatchBuilder body(final InputStream stream, final String contentType) {
        return (PatchBuilder) super.body(stream, contentType);
    }

    @Override
    public PatchBuilder ifMatch(final String etag) {
        return (PatchBuilder) super.ifMatch(etag);
    }

    @Override
    public PatchBuilder ifUnmodifiedSince(final String modified) {
        return (PatchBuilder) super.ifUnmodifiedSince(modified);
    }

    @Override
    public PatchBuilder ifStateToken(final String token) {
        return (PatchBuilder) super.ifStateToken(token);
    }

    @Deprecated
    @Override
    public PatchBuilder digest(final String digest) {
        return (PatchBuilder) super.digest(digest);
    }

    @Override
    public PatchBuilder digest(final String digest, final String alg) {
        return (PatchBuilder) super.digest(digest, alg);
    }

    @Override
    public PatchBuilder digestMd5(final String digest) {
        return (PatchBuilder) super.digestMd5(digest);
    }

    @Override
    public PatchBuilder digestSha1(final String digest) {
        return (PatchBuilder) super.digestSha1(digest);
    }

    @Override
    public PatchBuilder digestSha256(final String digest) {
        return (PatchBuilder) super.digestSha256(digest);
    }

    @Override
    public PatchBuilder addHeader(final String name, final String value) {
        return (PatchBuilder) super.addHeader(name, value);
    }

    @Override
    public PatchBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (PatchBuilder) super.addLinkHeader(linkHeader);
    }

    @Override
    public PatchBuilder addTransaction(final URI transaction) {
        return (PatchBuilder) super.addTransaction(transaction);
    }
}
