package dev.cambriota.identityprovider.tasks;

import org.jboss.logging.Logger;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import java.util.TimerTask;

/**
 * Some users might not want their data to be fully deleted from the database, but rather "anonymized".
 * Anonymization for sensitive fields (such as name, email) happens through hashing the value (using the DID as a salt).
 */
public class UserDeletionTask extends TimerTask {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private UserProvider userProvider;
    private RealmModel realm;
    private String username;

    public UserDeletionTask(UserProvider userProvider, RealmModel realm, String username) {
        this.userProvider = userProvider;
        this.realm = realm;
        this.username = username;
    }

    @Override
    public void run() {
        UserModel user = userProvider.getUserByUsername(realm, username);
        log.infof("Deleting user=[%s] ...", user.getUsername());

        boolean successful = userProvider.removeUser(realm, user);

        String did = user.getAttributeStream("did").toString();

        if (successful) {
            log.infof("User [%s] successfully deleted.", username);
        } else {
            log.errorf("User [%s] could not be deleted.", username);
        }
    }
}
