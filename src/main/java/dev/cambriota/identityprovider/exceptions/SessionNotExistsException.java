package dev.cambriota.identityprovider.exceptions;

import java.util.UUID;

public class SessionNotExistsException extends Exception {
    public SessionNotExistsException(UUID sessionId) {
        super("Pending session [id=" + sessionId + "] does not exist.");
    }
}
