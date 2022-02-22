package dev.cambriota.identityprovider.tasks;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.timer.ScheduledTask;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class UserDeletionScheduledTask implements ScheduledTask {

    private static final Logger log = Logger.getLogger(UserDeletionScheduledTask.class);
    private static final String REALM_NAME = "iota";

    public static final String TASK_NAME = "UserDeletionScheduledTask";

    @Override
    public void run(KeycloakSession session) {
        RealmModel realm = session.getProvider(RealmProvider.class).getRealmByName(REALM_NAME);

        log.debugf("Running scheduled task on users in realm=[%s]: %d", REALM_NAME, session.users().getUsersCount(realm));

        session.users().getUsersStream(realm, false)
                .filter(user -> hasAttribute(user, "requestDeletionAt"))
                .forEach(user -> {
                    LocalDateTime requestDeletionAt = LocalDateTime.parse(user.getFirstAttribute("requestDeletionAt"));
                    if (requestDeletionAt.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                        log.infof("Deleting user=[%s] due to requestDeletionAt=[%s]", user.getUsername(), requestDeletionAt);
                        boolean successful = session.users().removeUser(realm, user);
                        if (!successful) {
                            log.errorf("User [%s] could not be deleted.", user.getUsername());
                        }
                    }
                });
    }

    private boolean hasAttribute(UserModel user, String attributeName) {
        return user.getAttributeStream(attributeName).anyMatch(Objects::nonNull);
    }
}
