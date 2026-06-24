/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHttpRequest;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FcrepoHttpClientBuilder}.
 *
 * @author surfrdan
 */
public class FcrepoHttpClientBuilderTest {

    @Test
    public void testBuildWithNoCredentials() throws Exception {
        final FcrepoHttpClientBuilder builder = new FcrepoHttpClientBuilder(null, null, null);
        final CloseableHttpClient client = builder.build();
        assertNotNull(client);
    }

    @Test
    public void testBuildWithBlankPassword() throws Exception {
        final FcrepoHttpClientBuilder builder = new FcrepoHttpClientBuilder("user", "", "localhost");
        final CloseableHttpClient client = builder.build();
        assertNotNull(client);
    }

    @Test
    public void testBuildWithCredentialsNoHost() throws Exception {
        final FcrepoHttpClientBuilder builder = new FcrepoHttpClientBuilder("user", "password", null);
        final CloseableHttpClient client = builder.build();
        assertNotNull(client);
    }

    @Test
    public void testBuildWithCredentialsAndHost() throws Exception {
        final FcrepoHttpClientBuilder builder = new FcrepoHttpClientBuilder("user", "password", "localhost");
        final CloseableHttpClient client = builder.build();
        assertNotNull(client);
    }

    @Test
    public void testPreemptiveAuthInterceptorAddsAuthorizationHeader() throws Exception {
        final FcrepoHttpClientBuilder.PreemptiveAuthInterceptor interceptor =
                new FcrepoHttpClientBuilder.PreemptiveAuthInterceptor();

        final HttpHost targetHost = new HttpHost("http", "localhost", 8080);
        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost),
                new UsernamePasswordCredentials("user", "password".toCharArray()));

        final HttpClientContext context = new HttpClientContext();
        context.setCredentialsProvider(credsProvider);

        final BasicHttpRequest request = new BasicHttpRequest("GET", URI.create("http://localhost:8080/"));
        interceptor.process(request, null, context);

        assertNotNull(request.getFirstHeader(HttpHeaders.AUTHORIZATION),
                "A preemptive Authorization header should have been added");
        assertTrue(request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue().startsWith("Basic "),
                "The Authorization header should use the Basic scheme");
    }

    @Test
    public void testPreemptiveAuthInterceptorWithNullCredentials() throws Exception {
        final FcrepoHttpClientBuilder.PreemptiveAuthInterceptor interceptor =
                new FcrepoHttpClientBuilder.PreemptiveAuthInterceptor();

        final CredentialsProvider credsProvider = mock(CredentialsProvider.class);
        when(credsProvider.getCredentials(any(AuthScope.class), any())).thenReturn(null);

        final HttpClientContext context = new HttpClientContext();
        context.setCredentialsProvider(credsProvider);

        // With no credentials available for the target host, preemptive authentication cannot be initialized
        assertThrows(HttpException.class,
                () -> interceptor.process(new BasicHttpRequest("GET", URI.create("http://localhost:8080/")), null,
                        context));
    }

    @Test
    public void testPreemptiveAuthInterceptorSkipsWhenHeaderPresent() throws Exception {
        final FcrepoHttpClientBuilder.PreemptiveAuthInterceptor interceptor =
                new FcrepoHttpClientBuilder.PreemptiveAuthInterceptor();

        final CredentialsProvider credsProvider = mock(CredentialsProvider.class);

        final HttpClientContext context = new HttpClientContext();
        context.setCredentialsProvider(credsProvider);

        // The request already carries an Authorization header, so the interceptor has nothing to do
        final BasicHttpRequest request = new BasicHttpRequest("GET", URI.create("http://localhost:8080/"));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNzd29yZA==");
        interceptor.process(request, null, context);

        // Since a header was already present, the provider should never be consulted
        verify(credsProvider, never()).getCredentials(any(AuthScope.class), any());
    }
}
