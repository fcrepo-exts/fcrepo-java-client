/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.PREFER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ContentDisposition.Builder;

/**
 * Builds a PUT request for interacting with the Fedora HTTP API in order to create a resource with a specified path,
 * or replace the triples associated with a resource with the triples provided in the request body.
 *
 * @author bbpennel
 */
public class PutBuilder extends BodyRequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    public PutBuilder(final URI uri, final FcrepoClient client) {
        super(uri, client);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.PUT.createRequest(targetUri);
    }

    @Override
    public PutBuilder body(final InputStream stream, final String contentType) {
        return (PutBuilder) super.body(stream, contentType);
    }

    @Override
    public PutBuilder body(final File file, final String contentType) throws IOException {
        return (PutBuilder) super.body(file, contentType);
    }

    @Override
    public PutBuilder externalContent(final URI contentURI, final String contentType, final String handling) {
        return (PutBuilder) super.externalContent(contentURI, contentType, handling);
    }

    @Override
    public PutBuilder body(final InputStream stream) {
        return (PutBuilder) super.body(stream);
    }

    @Override
    public PutBuilder ifMatch(final String etag) {
        return (PutBuilder) super.ifMatch(etag);
    }

    @Override
    public PutBuilder ifUnmodifiedSince(final String modified) {
        return (PutBuilder) super.ifUnmodifiedSince(modified);
    }

    @Override
    public PutBuilder ifStateToken(final String token) {
        return (PutBuilder) super.ifStateToken(token);
    }

    @Deprecated
    @Override
    public PutBuilder digest(final String digest) {
        return (PutBuilder) super.digest(digest);
    }

    @Override
    public PutBuilder digest(final String digest, final String alg) {
        return (PutBuilder) super.digest(digest, alg);
    }

    @Override
    public PutBuilder digestMd5(final String digest) {
        return (PutBuilder) super.digestMd5(digest);
    }

    @Override
    public PutBuilder digestSha1(final String digest) {
        return (PutBuilder) super.digestSha1(digest);
    }

    @Override
    public PutBuilder digestSha256(final String digest) {
        return (PutBuilder) super.digestSha256(digest);
    }

    @Override
    public PutBuilder addInteractionModel(final String interactionModelUri) {
        return (PutBuilder) super.addInteractionModel(interactionModelUri);
    }

    @Override
    public PutBuilder linkAcl(final String aclUri) {
        return (PutBuilder) super.linkAcl(aclUri);
    }

    @Override
    public PutBuilder addHeader(final String name, final String value) {
        return (PutBuilder) super.addHeader(name, value);
    }

    @Override
    public PutBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (PutBuilder) super.addLinkHeader(linkHeader);
    }

    /**
     * Provide a content disposition header which will be used as the filename
     *
     * @param filename the name of the file being provided in the body of the request
     * @return this builder
     * @throws FcrepoOperationFailedException if unable to encode filename
     */
    public PutBuilder filename(final String filename) throws FcrepoOperationFailedException {
        final Builder builder = ContentDisposition.builder("attachment");
        if (filename != null) {
            builder.filename(filename);
        }
        request.addHeader(CONTENT_DISPOSITION, builder.build().toString());
        return this;
    }

    /**
     * Set the prefer header for this request to lenient handling, to indicate that server-managed triples will not
     * be included in the request body.
     *
     * @return this builder
     */
    public PutBuilder preferLenient() {
        request.setHeader(PREFER, "handling=lenient; received=\"minimal\"");
        return this;
    }
}
