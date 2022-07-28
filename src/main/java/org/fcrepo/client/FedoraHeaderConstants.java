/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

/**
 * Header constants used in calls to the Fedora API
 *
 * @author bbpennel
 */
public class FedoraHeaderConstants {

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String CONTENT_DISPOSITION_FILENAME = "filename";

    public static final String CONTENT_DISPOSITION_CREATION_DATE = "creation-date";

    public static final String CONTENT_DISPOSITION_MODIFICATION_DATE = "modification-date";

    public static final String CONTENT_DISPOSITION_SIZE = "size";

    public static final String SLUG = "Slug";

    public static final String DIGEST = "digest";

    public static final String LOCATION = "Location";

    public static final String ACCEPT = "Accept";

    public static final String PREFER = "Prefer";

    public static final String RANGE = "Range";

    public static final String IF_NONE_MATCH = "If-None-Match";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String IF_MATCH = "If-Match";

    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String ETAG = "ETag";

    public static final String STATE_TOKEN = "X-State-Token";

    public static final String IF_STATE_TOKEN = "X-If-State-Token";

    public static final String DESTINATION = "Destination";

    public static final String LINK = "Link";

    public static final String WANT_DIGEST = "Want-Digest";

    public static final String CACHE_CONTROL = "Cache-Control";

    /**
     * Datetime for a memento, either provided when creating the memento or returned when retrieving one.
     */
    public static final String MEMENTO_DATETIME = "Memento-Datetime";

    public static final String ACCEPT_DATETIME = "Accept-Datetime";

    public static final String ATOMIC_ID = "Atomic-ID";

    public static final String ATOMIC_EXPIRES = "Atomic-Expires";

    private FedoraHeaderConstants() {
    }
}
