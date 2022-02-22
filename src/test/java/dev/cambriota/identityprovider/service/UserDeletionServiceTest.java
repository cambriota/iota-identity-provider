package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.KeycloakTestBase;
import dev.cambriota.identityprovider.TestDataCreators;
import dev.cambriota.identityprovider.tasks.UserDeletionService;
import org.junit.jupiter.api.Test;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.models.*;
import org.keycloak.timer.TimerProvider;
import org.keycloak.timer.basic.BasicTimerProvider;
import org.keycloak.timer.basic.BasicTimerProviderFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Timer;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class UserDeletionServiceTest extends KeycloakTestBase {

    UserDeletionService cut = new UserDeletionService();

    protected final KeycloakSessionFactory keycloakSessionFactory = mock(KeycloakSessionFactory.class);
    protected final ClusterProvider clusterProvider = mock(ClusterProvider.class);
    protected final RealmProvider realmProvider = mock(RealmProvider.class);

    @Test
    void schedulesDeletionOfUser() throws Exception {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        System.setProperty("keycloak.scheduled.interval", String.valueOf(1L));

        UserModel userToBeDeleted = getRandomUser();
        userToBeDeleted.setUsername("bruce.wayne");
        userToBeDeleted.setSingleAttribute("requestDeletionAt", now.minusMinutes(30).toString());
        userToBeDeleted.setSingleAttribute("did", TestDataCreators.createTestDid());

        UserModel userNotToBeDeleted = getRandomUser();
        userNotToBeDeleted.setUsername("tony.stark");
        userNotToBeDeleted.setSingleAttribute("requestDeletionAt", now.plusHours(3).toString());

        when(session.users()).thenReturn(userProvider);

        BasicTimerProviderFactory basicTimerProviderFactory = new BasicTimerProviderFactory();
        when(session.getProvider(TimerProvider.class)).thenReturn(
                new BasicTimerProvider(session, new Timer(), 5, basicTimerProviderFactory)
        );

        Stream<UserModel> userStream = Stream.of(
                userToBeDeleted,
                userNotToBeDeleted,
                getRandomUser()
        );

        when(userProvider.getUsersStream(any(), anyBoolean())).thenReturn(userStream);

        when(session.getKeycloakSessionFactory()).thenReturn(keycloakSessionFactory);
        when(keycloakSessionFactory.create()).thenReturn(session);

        when(session.getTransactionManager()).thenReturn(mock(KeycloakTransactionManager.class));

        when(session.getProvider(ClusterProvider.class)).thenReturn(clusterProvider);

        when(session.getProvider(RealmProvider.class)).thenReturn(realmProvider);
        when(realmProvider.getRealmByName(anyString())).thenReturn(realm);

        cut.setupScheduledUserDeletion(keycloakSessionFactory);

        // wait until scheduled task has executed once
        Thread.sleep(2L * 1000);

        verify(userProvider).removeUser(realm, userToBeDeleted);
    }
}
