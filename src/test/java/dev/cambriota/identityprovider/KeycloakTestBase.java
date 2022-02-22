package dev.cambriota.identityprovider;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.storage.adapter.InMemoryUserAdapter;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeProvider;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Provides a test base for Keycloak unit tests where most parts are mocked.
 */
public abstract class KeycloakTestBase {
    protected final KeycloakSession session = mock(KeycloakSession.class);
    protected final HttpRequest request = mock(HttpRequest.class);
    protected final RealmModel realm = mock(RealmModel.class);
    protected final UserProvider userProvider = mock(UserProvider.class);
    protected final UserCredentialManager userCredentialManager = mock(UserCredentialManager.class);
    protected final EventBuilder eventBuilder = mock(EventBuilder.class);
    protected final ThemeProvider themeProvider = mock(ThemeProvider.class);
    protected final Theme theme = mock(Theme.class);
    protected final KeycloakContext context = mock(KeycloakContext.class);
    protected final HttpClientProvider httpClientProvider = mock(HttpClientProvider.class);
    protected final CloseableHttpClient httpClient = mock(CloseableHttpClient.class);

    protected KeycloakTestBase() {
        when(session.userLocalStorage()).thenReturn(userProvider);
        when(session.userCredentialManager()).thenReturn(userCredentialManager);
        when(session.getContext()).thenReturn(context);

        when(context.getRealm()).thenReturn(realm);

        when(realm.getLoginTheme()).thenReturn("keywind");

        when(eventBuilder.user(anyString())).thenReturn(eventBuilder);

        when(userProvider.getUserById(eq(realm), anyString())).thenReturn(getRandomUser());

        when(session.getProvider(ThemeProvider.class, "extending")).thenReturn(themeProvider);

        when(session.getProvider(HttpClientProvider.class)).thenReturn(httpClientProvider);

        try {
            when(themeProvider.getTheme(anyString(), any(Theme.Type.class))).thenReturn(theme);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected UserModel getRandomUser() {
        return spy(new InMemoryUserAdapter(session, realm, UUID.randomUUID().toString()));
    }
}
