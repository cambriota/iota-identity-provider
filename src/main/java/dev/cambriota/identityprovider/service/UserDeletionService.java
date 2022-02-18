package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.tasks.UserDeletionTask;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Timer;

public class UserDeletionService {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final KeycloakSession session;

    public UserDeletionService(KeycloakSession session) {
        this.session = session;
    }

    public void scheduleDeletion(String username, LocalDateTime deleteAt) {
        new Timer().schedule(
                new UserDeletionTask(session.users(), session.getContext().getRealm(), username),
                Date.from(deleteAt.atZone(ZoneOffset.UTC).toInstant())
        );
        log.infof("Scheduled deletion of user=[%s] at %s", username, deleteAt);
    }
}
