/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.slf4j.LoggerFactory.getLogger;
import static org.fcrepo.client.FedoraHeaderConstants.LINK;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.Args;
import org.slf4j.Logger;

/**
 * Base RequestBuilder class for constructing requests to the Fedora API
 *
 * @author bbpennel
 */
public abstract class RequestBuilder {

    private static final Logger LOGGER = getLogger(RequestBuilder.class);

    // Fedora client which will make this request
    protected FcrepoClient client;

    // URL this request will be executed against
    protected URI targetUri;

    // The request being built
    protected HttpRequestBase request;

    /**
     * Instantiate builder. Throws an IllegalArgumentException if either the uri or client are null.
     *
     * @param uri uri of the resource this request is being made to
     * @param client the client
     */
    protected RequestBuilder(final URI uri, final FcrepoClient client) {
        Args.notNull(uri, "uri");
        Args.notNull(client, "client");

        this.targetUri = uri;
        this.client = client;
        this.request = createRequest();
    }

    /**
     * Creates the HTTP request object for this builder
     *
     * @return HTTP request object for this builder
     */
    protected abstract HttpRequestBase createRequest();

    /**
     * Performs the request constructed in this builder and returns the response
     *
     * @return the repository response
     * @throws FcrepoOperationFailedException when the underlying HTTP request results in an error
     */
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        LOGGER.debug("Fcrepo {} request to {} with headers: {}", request.getMethod(), targetUri,
                request.getAllHeaders());

        return client.executeRequest(targetUri, request);
    }

    /**
     * Add a header with the given name and value to the request.
     *
     * @param name name of the header
     * @param value value of the header
     * @return this builder
     */
    protected RequestBuilder addHeader(final String name, final String value) {
        request.addHeader(name, value);
        return this;
    }

    /**
     * Add a link header to the request
     *
     * @param linkHeader link header value represented as a FcrepoLink
     * @return this builder
     */
    protected RequestBuilder addLinkHeader(final FcrepoLink linkHeader) {
        request.addHeader(LINK, linkHeader.toString());
        return this;
    }
}
