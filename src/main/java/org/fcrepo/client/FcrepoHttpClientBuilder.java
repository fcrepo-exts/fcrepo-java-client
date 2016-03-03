/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
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

            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            AuthScope scope = null;

            if (isBlank(host)) {
                scope = new AuthScope(AuthScope.ANY);
            } else {
                scope = new AuthScope(new HttpHost(host));
            }
            credsProvider.setCredentials(
                    scope,
                    new UsernamePasswordCredentials(username, password));
            return HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .useSystemProperties()
                    .addInterceptorFirst(new PreemptiveAuthInterceptor())
                    .build();
        }
    }

    static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            final AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
            // If no auth scheme available yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                final CredentialsProvider credsProvider = (CredentialsProvider)
                        context.getAttribute(HttpClientContext.CREDS_PROVIDER);
                final HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                final AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                final Credentials creds = credsProvider.getCredentials(authScope);
                if (creds == null) {
                    LOGGER.debug("Cannot initiate preemtive authentication, Credentials not found!");
                }
                authState.update(new BasicScheme(), creds);
            }
        }
    }
}
