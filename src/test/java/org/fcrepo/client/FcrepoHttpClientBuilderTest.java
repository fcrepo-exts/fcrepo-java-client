/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link FcrepoHttpClientBuilder}.
 *
 * @author surfrdan
 */
@RunWith(MockitoJUnitRunner.class)
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
    public void testPreemptiveAuthInterceptorInitializesAuthScheme() throws Exception {
        final FcrepoHttpClientBuilder.PreemptiveAuthInterceptor interceptor =
                new FcrepoHttpClientBuilder.PreemptiveAuthInterceptor();

        final HttpHost targetHost = new HttpHost("localhost", 8080);
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope("localhost", 8080),
                new UsernamePasswordCredentials("user", "password"));

        final AuthState authState = new AuthState();
        final HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.TARGET_AUTH_STATE, authState);
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, targetHost);

        interceptor.process(new BasicHttpRequest("GET", "/"), context);

        assertNotNull("Auth scheme should be initialized preemptively", authState.getAuthScheme());
        assertNotNull("Credentials should be set on the auth state", authState.getCredentials());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreemptiveAuthInterceptorWithNullCredentials() throws Exception {
        final FcrepoHttpClientBuilder.PreemptiveAuthInterceptor interceptor =
                new FcrepoHttpClientBuilder.PreemptiveAuthInterceptor();

        final HttpHost targetHost = new HttpHost("localhost", 8080);
        final CredentialsProvider credsProvider = mock(CredentialsProvider.class);
        when(credsProvider.getCredentials(any(AuthScope.class))).thenReturn(null);

        final AuthState authState = new AuthState();
        final HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.TARGET_AUTH_STATE, authState);
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, targetHost);

        // With no credentials available, the interceptor attempts to update the auth state with a null
        // credential, which BasicScheme rejects.
        interceptor.process(new BasicHttpRequest("GET", "/"), context);
    }

    @Test
    public void testPreemptiveAuthInterceptorSkipsWhenSchemePresent() throws Exception {
        final FcrepoHttpClientBuilder.PreemptiveAuthInterceptor interceptor =
                new FcrepoHttpClientBuilder.PreemptiveAuthInterceptor();

        final CredentialsProvider credsProvider = mock(CredentialsProvider.class);
        final Credentials existingCreds = new UsernamePasswordCredentials("user", "password");

        final AuthState authState = new AuthState();
        authState.update(new BasicScheme(), existingCreds);

        final HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.TARGET_AUTH_STATE, authState);
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);

        interceptor.process(new BasicHttpRequest("GET", "/"), context);

        // Since a scheme was already present, the provider should never be consulted
        verify(credsProvider, never()).getCredentials(any(AuthScope.class));
    }
}
