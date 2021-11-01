/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.fcrepo.client.HeaderHelpers.UTC_RFC_1123_FORMATTER;
import static org.fcrepo.client.FedoraHeaderConstants.MEMENTO_DATETIME;

import java.net.URI;
import java.time.Instant;

/**
 * Builds a POST request for creating a memento (LDPRm) with the state given in the request body
 * and the datetime given in the Memento-Datetime request header.
 *
 * @author bbpennel
 */
public class HistoricMementoBuilder extends PostBuilder {

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
}
