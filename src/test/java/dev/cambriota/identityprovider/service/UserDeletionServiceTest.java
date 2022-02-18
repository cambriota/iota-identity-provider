package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.KeycloakTestBase;
import dev.cambriota.identityprovider.TestDataCreators;
import org.junit.jupiter.api.Test;
import org.keycloak.models.UserModel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserDeletionServiceTest extends KeycloakTestBase {

    UserDeletionService cut = new UserDeletionService(session);

    @Test
    void schedulesAnonymizationForUser() throws Exception {
        String username = "johndoe";
        LocalDateTime anonymizeAt = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1);

        UserModel user = getRandomUser();
        user.setUsername(username);
        user.setFirstName("John");
        user.setSingleAttribute("did", TestDataCreators.createTestDid());

        when(session.users()).thenReturn(userProvider);
        when(userProvider.getUserByUsername(eq(realm), anyString())).thenReturn(user);

        cut.scheduleDeletion(username, anonymizeAt);

        while(LocalDateTime.now(ZoneOffset.UTC).isBefore(anonymizeAt)) {
            verify(userProvider, never()).getUserByUsername(any(), anyString());
            Thread.sleep(500);
        }

        verify(userProvider).getUserByUsername(realm, username);
    }

    @Test
    public void givenUsingTimer_whenSchedulingTaskOnce_thenCorrect() {
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on: " + new Date() + "n" +
                        "Thread's name: " + Thread.currentThread().getName());
            }
        };
        Timer timer = new Timer("Timer");

        long delay = 1000L;
        timer.schedule(task, delay);
    }
}
