/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.app.authentication;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStoreException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.JAXBException;

import com.github.triceo.robozonky.ApiProvider;
import com.github.triceo.robozonky.api.remote.ZonkyApi;
import com.github.triceo.robozonky.api.remote.ZonkyOAuthApi;
import com.github.triceo.robozonky.api.remote.entities.ZonkyApiToken;
import com.github.triceo.robozonky.app.util.KeyStoreHandler;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class AuthenticationHandlerTest {

    private static final ZonkyApiToken TOKEN = new ZonkyApiToken(UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), 299);

    private static SecretProvider getNewProvider() throws IOException, KeyStoreException {
        final File file = File.createTempFile("robozonky-", ".keystore");
        file.delete();
        final String username = "user";
        final char[] password = "pass".toCharArray();
        final KeyStoreHandler ksh = KeyStoreHandler.create(file, password);
        return SecretProvider.keyStoreBased(ksh, username, password);
    }

    private static SecretProvider mockExistingProvider(final OffsetDateTime storedOn,
                                                       final boolean succeedInSavingTokens,
                                                       final boolean succeedInDeletingToken) throws JAXBException {
        return AuthenticationHandlerTest.mockExistingProvider(ZonkyApiToken.marshal(AuthenticationHandlerTest.TOKEN),
                storedOn, succeedInSavingTokens, succeedInDeletingToken);
    }

    private static SecretProvider mockExistingProvider(final String token, final OffsetDateTime storedOn,
                                                       final boolean succeedInSavingTokens,
                                                       final boolean succeedInDeletingToken) throws JAXBException {
        final SecretProvider p = Mockito.mock(SecretProvider.class);
        Mockito.when(p.getPassword()).thenReturn(new char[0]);
        Mockito.when(p.getToken()).then(invocation -> Optional.of(new StringReader(token)));
        Mockito.when(p.getTokenSetDate()).thenReturn(Optional.of(storedOn));
        Mockito.when(p.setToken(ArgumentMatchers.any())).thenReturn(succeedInSavingTokens);
        Mockito.when(p.deleteToken()).thenReturn(succeedInDeletingToken);
        return p;
    }

    @Test
    public void simplePasswordBased() throws IOException, KeyStoreException {
        final SecretProvider secrets = AuthenticationHandlerTest.getNewProvider();
        final AuthenticationHandler auth = AuthenticationHandler.passwordBased(secrets);
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        final ZonkyOAuthApi zonkyOauth = Mockito.mock(ZonkyOAuthApi.class);
        final ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        Mockito.when(apiProvider.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        Mockito.when(apiProvider.oauth()).thenReturn(zonkyOauth);
        Assertions.assertThat(auth.getSecretProvider()).isSameAs(secrets);
        Assertions.assertThat(auth.execute(apiProvider, (api) -> Collections.emptyList())).isPresent();
        Mockito.verify(zonkyOauth, Mockito.times(1))
                .login(ArgumentMatchers.eq(secrets.getUsername()),
                        ArgumentMatchers.eq(new String(secrets.getPassword())), ArgumentMatchers.any(),
                        ArgumentMatchers.any());
        Mockito.verify(zonkyOauth, Mockito.never())
                .refresh(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonky, Mockito.times(1)).logout();
    }

    @Test
    public void simpleTokenBasedWithExistingToken() throws JAXBException {
        final SecretProvider secretProvider = AuthenticationHandlerTest.mockExistingProvider(OffsetDateTime.now(),
                true, true);
        final AuthenticationHandler h = AuthenticationHandler.tokenBased(secretProvider, Duration.ofSeconds(60));
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        final ZonkyOAuthApi zonkyOauth = Mockito.mock(ZonkyOAuthApi.class);
        final ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        Mockito.when(apiProvider.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        Mockito.when(apiProvider.oauth()).thenReturn(zonkyOauth);
        Assertions.assertThat(h.execute(apiProvider, (api) -> Collections.emptyList())).isPresent();
        Mockito.verify(zonkyOauth, Mockito.never())
                .refresh(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonkyOauth, Mockito.never())
                .login(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(zonky, Mockito.never()).logout();
    }

    @Test
    public void simpleTokenBasedWithoutToken() throws JAXBException, IOException, KeyStoreException {
        final SecretProvider secrets = AuthenticationHandlerTest.getNewProvider();
        final AuthenticationHandler auth = AuthenticationHandler.tokenBased(secrets, Duration.ofSeconds(60));
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        final ZonkyOAuthApi zonkyOauth = Mockito.mock(ZonkyOAuthApi.class);
        final ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        Mockito.when(apiProvider.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        Mockito.when(apiProvider.oauth()).thenReturn(zonkyOauth);
        Assertions.assertThat(auth.execute(apiProvider, (api) -> Collections.emptyList())).isPresent();
        Mockito.verify(zonkyOauth, Mockito.never())
                .refresh(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonkyOauth, Mockito.times(1))
                .login(ArgumentMatchers.eq(secrets.getUsername()),
                        ArgumentMatchers.eq(new String(secrets.getPassword())), ArgumentMatchers.any(),
                        ArgumentMatchers.any());
        Mockito.verify(zonky, Mockito.times(1)).logout();
    }

    @Test
    public void tokenBasedWithExpiredToken() throws IOException, KeyStoreException, JAXBException {
        final OffsetDateTime expired =
                OffsetDateTime.now().minus(AuthenticationHandlerTest.TOKEN.getExpiresIn() + 1, ChronoUnit.SECONDS);
        final SecretProvider secrets = AuthenticationHandlerTest.mockExistingProvider(expired, true, true);
        final AuthenticationHandler auth = AuthenticationHandler.tokenBased(secrets, Duration.ofSeconds(60));
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        final ZonkyOAuthApi zonkyOauth = Mockito.mock(ZonkyOAuthApi.class);
        final ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        Mockito.when(apiProvider.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        Mockito.when(apiProvider.oauth()).thenReturn(zonkyOauth);
        Assertions.assertThat(auth.execute(apiProvider, (api) -> Collections.emptyList())).isPresent();
        Mockito.verify(zonkyOauth, Mockito.never())
                .refresh(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonkyOauth, Mockito.times(1))
                .login(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonky, Mockito.never()).logout();
    }

    @Test
    public void tokenBasedWithExpiringToken() throws JAXBException {
        final OffsetDateTime expiring =
                OffsetDateTime.now().minus(AuthenticationHandlerTest.TOKEN.getExpiresIn() - 1, ChronoUnit.SECONDS);
        final SecretProvider secrets = AuthenticationHandlerTest.mockExistingProvider(expiring, true, true);
        final AuthenticationHandler auth = AuthenticationHandler.tokenBased(secrets, Duration.ofSeconds(60));
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        final ZonkyOAuthApi zonkyOauth = Mockito.mock(ZonkyOAuthApi.class);
        final ApiProvider apiProvider = Mockito.mock(ApiProvider.class);
        Mockito.when(apiProvider.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        Mockito.when(apiProvider.oauth()).thenReturn(zonkyOauth);
        Assertions.assertThat(auth.execute(apiProvider, (api) -> Collections.emptyList())).isPresent();
        Mockito.verify(zonkyOauth, Mockito.times(1))
                .refresh(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonkyOauth, Mockito.never())
                .login(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(zonky, Mockito.never()).logout();
    }

    @Test
    public void failWhileStoringToken() throws JAXBException {
        final SecretProvider provider = Mockito.mock(SecretProvider.class);
        Mockito.when(provider.setToken(ArgumentMatchers.any())).thenReturn(false);
        final AuthenticationHandler auth = AuthenticationHandler.passwordBased(provider);
        Assertions.assertThat(auth.storeToken(AuthenticationHandlerTest.TOKEN)).isFalse();
    }

}
