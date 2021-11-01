/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client.integration;

import java.util.concurrent.TimeUnit;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoResponse;

/**
 * @author bbpennel
 */
public abstract class AbstractResourceIT {

    protected static final int SERVER_PORT = Integer.parseInt(System
            .getProperty("fcrepo.dynamic.test.port", "8080"));

    protected static final String HOSTNAME = "localhost";

    protected static final String serverAddress = "http://" + HOSTNAME + ":" +
            SERVER_PORT + "/fcrepo/rest/";

    protected final PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager();

    protected static FcrepoClient client;

    protected AbstractResourceIT() {
        connectionManager.setMaxTotal(Integer.MAX_VALUE);
        connectionManager.setDefaultMaxPerRoute(20);
        connectionManager.closeIdleConnections(3, TimeUnit.SECONDS);
    }

    protected Model getResponseModel(final FcrepoResponse resp) {
        final Model model = ModelFactory.createDefaultModel();
        model.read(resp.getBody(), null, "text/turtle");
        return model;
    }
}
