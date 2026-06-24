/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;

/**
 * A utility class for building an httpclient for interacting with a Fedora repository
 *
 * @author Aaron Coburn
 * @since March 9, 2015
 */
public class FcrepoHttpClientBuilder {

    private String username;

    private String password;

    private String host;

    private static final Logger LOGGER = getLogger(FcrepoHttpClientBuilder.class);

    /**
     * Create a FcrepoHttpClientBuilder object with which it is possible to create
     * an HttpClient object
     *
     * @param username an optional username for authentication
     * @param password an optional password for authentication
     * @param host an optional realm for authentication
     */
    public FcrepoHttpClientBuilder(final String username, final String password, final String host) {
        this.username = username;
        this.password = password;
        this.host = host;
    }

    /**
     *  Build an HttpClient
     *
     *  @return an HttpClient
     */
    public CloseableHttpClient build() {

        if (isBlank(username) || isBlank(password)) {
            return HttpClients.createSystem();
        } else {
            LOGGER.debug("Accessing fcrepo with user credentials");

            final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            final AuthScope scope;

            if (isBlank(host)) {
                // match any host/port/realm
                scope = new AuthScope(null, null, -1, null, null);
            } else {
                scope = new AuthScope(new HttpHost(host));
            }
            credsProvider.setCredentials(
                    scope,
                    new UsernamePasswordCredentials(username, password.toCharArray()));
            return HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .useSystemProperties()
                    .addRequestInterceptorFirst(new PreemptiveAuthInterceptor())
                    .build();
        }
    }

    static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
                throws HttpException, IOException {
            // If the request already carries an Authorization header, leave it untouched
            if (request.containsHeader(HttpHeaders.AUTHORIZATION)) {
                return;
            }
            final HttpClientContext clientContext = HttpClientContext.cast(context);
            final HttpHost targetHost = RoutingSupport.determineHost(request);
            final AuthScope authScope = new AuthScope(targetHost);
            final Credentials creds = clientContext.getCredentialsProvider().getCredentials(authScope, context);
            if (creds == null) {
                LOGGER.debug("Cannot initiate preemptive authentication, Credentials not found!");
                throw new HttpException("Credentials not found for preemptive authentication");
            }
            // Generate the Basic Authorization header up front so it is sent on the first request. The
            // credentials always carry a password here, since build() only installs this interceptor when a
            // non-blank password was supplied.
            final String token = creds.getUserPrincipal().getName() + ":" + new String(creds.getPassword());
            final String authHeader = "Basic " +
                    Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }
}
