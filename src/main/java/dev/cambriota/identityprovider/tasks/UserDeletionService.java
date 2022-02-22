package dev.cambriota.identityprovider.tasks;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.scheduled.ScheduledTaskRunner;
import org.keycloak.timer.TimerProvider;

public class UserDeletionService {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    public void setupScheduledUserDeletion(final KeycloakSessionFactory sessionFactory) {
        long interval = Config.scope("scheduled").getLong("interval", 10L) * 1000;

        KeycloakSession session = sessionFactory.create();

        try {
            TimerProvider timer = session.getProvider(TimerProvider.class);
            log.infof("Starting scheduled user deletion task with interval=[%ds] ...", (int) interval / 1000);
            // timer.schedule(new ClusterAwareScheduledTaskRunner(sessionFactory, new UserDeletionScheduledTask(username), deleteInSeconds * 1000), deleteInSeconds * 1000, "mytaskname");
            timer.schedule(new ScheduledTaskRunner(sessionFactory, new UserDeletionScheduledTask()), interval, UserDeletionScheduledTask.TASK_NAME);
            // new UserStorageSyncManager().bootstrapPeriodic(sessionFactory, timer);
        } finally {
            session.close();
        }
    }
}
