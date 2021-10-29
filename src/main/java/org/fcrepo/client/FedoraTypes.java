/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

/**
 * Helper constants for resource types used in the Fedora specification.
 *
 * @author bbpennel
 */
public class FedoraTypes {

    // Type representing an LDP resource
    public static final String LDP_RESOURCE = "http://www.w3.org/ns/ldp#Resource";

    // Type representing an LDP non-RDF source/binary
    public static final String LDP_NON_RDF_SOURCE = "http://www.w3.org/ns/ldp#NonRDFSource";

    // Type representing an LDP basic container
    public static final String LDP_BASIC_CONTAINER = "http://www.w3.org/ns/ldp#BasicContainer";

    // Type representing an LDP direct container
    public static final String LDP_DIRECT_CONTAINER = "http://www.w3.org/ns/ldp#DirectContainer";

    // Type representing an LDP indirect container
    public static final String LDP_INDIRECT_CONTAINER = "http://www.w3.org/ns/ldp#IndirectContainer";

    // Type representing a Memento TimeGate
    public final static String MEMENTO_TIME_GATE_TYPE = "http://mementoweb.org/ns#TimeGate";

    // Type representing a Memento TimeMap (LDPCv)
    public final static String MEMENTO_TIME_MAP_TYPE = "http://mementoweb.org/ns#TimeMap";

    // Type representing a Memento original resource (LDPRv)
    public final static String MEMENTO_ORIGINAL_TYPE = "http://mementoweb.org/ns#OriginalResource";

    // Type representing a Memento (LDPRm)
    public final static String MEMENTO_TYPE = "http://mementoweb.org/ns#Memento";

    private FedoraTypes() {
    }
}
