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

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.fcrepo.client.FedoraHeaderConstants.MEMENTO_DATETIME;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Builds a POST request for creating a memento (LDPRm) with the state given in the request body
 * and the datetime given in the Memento-Datetime request header.
 *
 * @author bbpennel
 */
public class HistoricMementoBuilder extends PostBuilder {

    private static DateTimeFormatter UTC_RFC_1123_FORMATTER = RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC"));

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
     * @param uri
     * @param client
     * @param mementoDatetime RFC1123 formatted date to use for the memento-datetime
     */
    public HistoricMementoBuilder(final URI uri, final FcrepoClient client, final String mementoDatetime) {
        super(uri, client);
        // Parse the datetime to ensure that it is in RFC1123 format
        UTC_RFC_1123_FORMATTER.parse(mementoDatetime);
        request.setHeader(MEMENTO_DATETIME, mementoDatetime);
    }
}
