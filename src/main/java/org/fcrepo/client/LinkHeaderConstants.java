/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

/**
 * Link header values used in the Fedora specification.
 *
 * @author bbpennel
 */
public class LinkHeaderConstants {

    // handling parameter for external content Links
    public static final String EXTERNAL_CONTENT_HANDLING = "handling";

    // rel value for external content URI for binaries
    public static final String EXTERNAL_CONTENT_REL = "http://fedora.info/definitions/fcrepo#ExternalContent";

    // rel for identifying the ldpcv for a ldprv
    public static final String MEMENTO_TIME_MAP_REL = "timemap";

    // rel for identifying the timegate of an ldprv
    public static final String MEMENTO_TIME_GATE_REL = "timegate";

    // rel for identifying an ldprv
    public static final String MEMENTO_ORIGINAL_REL = "original";

    // rel for link header representing the type or interaction model of the object
    public static final String TYPE_REL = "type";

    // rel identifying the RDF resource describing this resource
    public static final String DESCRIBEDBY_REL = "describedby";

    // rel identifying the ACL for the resource
    public static final String ACL_REL = "acl";

    private LinkHeaderConstants() {
    }
}
