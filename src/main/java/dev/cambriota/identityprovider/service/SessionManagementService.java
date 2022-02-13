package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.exceptions.SessionNotExistsException;
import dev.cambriota.identityprovider.model.Subject;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.UUID;

/**
 * Service that stores "pending sessions" in a local HashMap. A validated subject will be appended to a pending session
 * and later retrieved for injection in the database.
 */
public class SessionManagementService {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private static SessionManagementService SINGLETON;

    private static final int MAX_SESSIONS = 99;
    private final HashMap<UUID, Subject> pendingSessions = new HashMap<>();

    public static SessionManagementService getInstance() {
        if (SINGLETON == null) {
            SINGLETON = new SessionManagementService();
        }
        return SINGLETON;
    }

    public String createNewSessionForId(String id) {
        UUID uuid = UUID.fromString(id);
        if (pendingSessions.size() <= MAX_SESSIONS) {
            if (!pendingSessions.containsKey(uuid)) {
                pendingSessions.put(uuid, null);
                log.infof("[+] Authentication session[id=%s] waiting for data ...", uuid);
            }
            return id;
        } else {
            log.warnf("[!] Number of max. parallel login sessions reached: %s", MAX_SESSIONS);
            // TODO: throw Exception and remove return value
            return null;
        }
    }

    public Boolean sessionExists(UUID sessionId) {
        return pendingSessions.containsKey(sessionId);
    }

    public Subject getSubjectBySessionId(UUID sessionId) throws SessionNotExistsException {
        if (sessionExists(sessionId)) {
            Subject sub = pendingSessions.get(sessionId);
            if (sub != null) {
                log.infof("Found attached subject for session[id=%s]: %s", sessionId, sub);
            } else {
                log.infof("No subject has been attached to pending session[id=%s]", sessionId);
            }
            return sub;
        } else {
            log.infof("No pending session found for [id=%s]", sessionId);
            throw new SessionNotExistsException(sessionId);
        }
    }

    public void attachSubjectToSession(UUID sessionId, Subject sub) throws SessionNotExistsException {
        if (sessionExists(sessionId)) {
            pendingSessions.put(sessionId, sub);
            log.infof("Attached subject=[%s] to session[id=%s]", sub, sessionId);
        } else {
            log.infof("No pending session found for [id=%s]", sessionId);
            throw new SessionNotExistsException(sessionId);
        }
    }

    public void destroySession(UUID sessionId) {
        pendingSessions.remove(sessionId);
        log.infof("[-] Session[id=%s] destroyed", sessionId);
    }
}
