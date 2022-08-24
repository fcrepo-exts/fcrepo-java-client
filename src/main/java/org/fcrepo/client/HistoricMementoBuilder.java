/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.fcrepo.client.FedoraHeaderConstants.CONTENT_DISPOSITION;
import static org.fcrepo.client.FedoraHeaderConstants.SLUG;
import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;
import static org.fcrepo.client.FedoraHeaderConstants.MEMENTO_DATETIME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;

import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.ContentDisposition;

/**
 * Builds a POST request for creating a memento (LDPRm) with the state given in the request body
 * and the datetime given in the Memento-Datetime request header.
 *
 * @author bbpennel
 */
public class HistoricMementoBuilder extends BodyRequestBuilder {

    /**
     * Instantiate builder
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     * @param mementoInstant Instant to use for the memento-datetime
     */
    public HistoricMementoBuilder(final URI uri, final FcrepoClient client, final Instant mementoInstant) {
        super(uri, client);
        final String rfc1123Datetime = UTC_RFC_1123_FORMATTER.format(mementoInstant);
        request.setHeader(MEMENTO_DATETIME, rfc1123Datetime);
    }

    /**
     * Instantiate builder.
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     * @param mementoDatetime RFC1123 formatted date to use for the memento-datetime
     */
    public HistoricMementoBuilder(final URI uri, final FcrepoClient client, final String mementoDatetime) {
        super(uri, client);
        // Parse the datetime to ensure that it is in RFC1123 format
        UTC_RFC_1123_FORMATTER.parse(mementoDatetime);
        request.setHeader(MEMENTO_DATETIME, mementoDatetime);
    }

    @Override
    protected HttpRequestBase createRequest() {
        return HttpMethods.POST.createRequest(targetUri);
    }

    @Override
    public HistoricMementoBuilder body(final InputStream stream, final String contentType) {
        return (HistoricMementoBuilder) super.body(stream, contentType);
    }

    @Override
    public HistoricMementoBuilder body(final File file, final String contentType) throws IOException {
        return (HistoricMementoBuilder) super.body(file, contentType);
    }

    @Override
    public HistoricMementoBuilder body(final InputStream stream) {
        return (HistoricMementoBuilder) super.body(stream);
    }

    @Override
    public HistoricMementoBuilder externalContent(final URI contentURI,
                                                  final String contentType,
                                                  final String handling) {
        return (HistoricMementoBuilder) super.externalContent(contentURI, contentType, handling);
    }

    @Override
    public HistoricMementoBuilder digest(final String digest, final String alg) {
        return (HistoricMementoBuilder) super.digest(digest, alg);
    }

    @Override
    public HistoricMementoBuilder digestMd5(final String digest) {
        return (HistoricMementoBuilder) super.digestMd5(digest);
    }

    @Override
    public HistoricMementoBuilder digestSha1(final String digest) {
        return (HistoricMementoBuilder) super.digestSha1(digest);
    }

    @Override
    public HistoricMementoBuilder digestSha256(final String digest) {
        return (HistoricMementoBuilder) super.digestSha256(digest);
    }

    @Override
    public HistoricMementoBuilder addInteractionModel(final String interactionModelUri) {
        return (HistoricMementoBuilder) super.addInteractionModel(interactionModelUri);
    }

    @Override
    public HistoricMementoBuilder linkAcl(final String aclUri) {
        return (HistoricMementoBuilder) super.linkAcl(aclUri);
    }

    @Override
    public HistoricMementoBuilder addHeader(final String name, final String value) {
        return (HistoricMementoBuilder) super.addHeader(name, value);
    }

    @Override
    public HistoricMementoBuilder addLinkHeader(final FcrepoLink linkHeader) {
        return (HistoricMementoBuilder) super.addLinkHeader(linkHeader);
    }

    /**
     * Provide a content disposition header which will be used as the filename
     *
     * @param filename the name of the file being provided in the body of the request
     * @return this builder
     * @throws FcrepoOperationFailedException if unable to encode filename
     */
    public HistoricMementoBuilder filename(final String filename) throws FcrepoOperationFailedException {
        final ContentDisposition.Builder builder = ContentDisposition.builder("attachment");
        if (filename != null) {
            builder.filename(filename);
        }
        request.addHeader(CONTENT_DISPOSITION, builder.build().toString());
        return this;
    }

    /**
     * Provide a suggested name for the new child resource, which the repository may ignore.
     *
     * @param slug value to supply as the slug header
     * @return this builder
     */
    public HistoricMementoBuilder slug(final String slug) {
        if (slug != null) {
            request.addHeader(SLUG, slug);
        }
        return this;
    }

}
