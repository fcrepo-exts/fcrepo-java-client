
package org.fcrepo.client;

import static org.fcrepo.client.FedoraHeaderConstants.DESTINATION;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

/**
 * Builds a request to move a resource (and its subtree) to a new location
 * 
 * @author bbpennel
 */
public class MoveBuilder<T extends MoveBuilder<T>> extends RequestBuilder<MoveBuilder<T>> {
    private static final Logger LOGGER = getLogger(MoveBuilder.class);

    protected URI destinationUrl;

    protected MoveBuilder(URI sourceUrl, URI destinationUrl, FcrepoClient client) {
        super(sourceUrl, client);
        this.destinationUrl = destinationUrl;
    }

    @Override
    public FcrepoResponse perform() throws FcrepoOperationFailedException {
        final HttpRequestBase request = HttpMethods.MOVE.createRequest(targetUri);

        if (destinationUrl != null) {
            request.addHeader(DESTINATION, destinationUrl.toString());
        }

        LOGGER.debug("Fcrepo MOVE request of {} to {}", targetUri, destinationUrl);

        return client.executeRequest(targetUri, request);
    }

    @Override
    protected MoveBuilder<T> self() {
        return this;
    }
}
