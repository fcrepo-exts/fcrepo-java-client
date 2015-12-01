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

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
            return HttpClients.createDefault();
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
                    .build();
        }
    }
}
