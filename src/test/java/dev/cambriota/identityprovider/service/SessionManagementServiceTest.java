package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.KeycloakTestBase;
import dev.cambriota.identityprovider.exceptions.SessionNotExistsException;
import dev.cambriota.identityprovider.model.Subject;
import dev.cambriota.identityprovider.TestDataCreators;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SessionManagementServiceTest extends KeycloakTestBase {

    SessionManagementService cut = new SessionManagementService();

    @Test
    void successfullyOpensAPendingSession() {
        UUID id = UUID.randomUUID();
        assertThat(cut.sessionExists(id)).isFalse();
        String created = cut.createNewSessionForId(id.toString());
        assertThat(cut.sessionExists(id)).isTrue();
        assertThat(created).isNotNull();
    }

    @Test
    void doesNotOpenANewSessionIfTooMany() {
        for (int i = 0; i < 100; i++) {
            UUID id = UUID.randomUUID();
            cut.createNewSessionForId(id.toString());
        }
        UUID id = UUID.randomUUID();
        String created = cut.createNewSessionForId(id.toString());
        assertThat(cut.sessionExists(id)).isFalse();
        assertThat(created).isNull();
    }

    @Test
    void successfullyAttachesASubjectToAPendingSession() throws Exception {
        UUID id = UUID.randomUUID();
        cut.createNewSessionForId(id.toString());
        cut.attachSubjectToSession(id, TestDataCreators.createTestSubject());
        Subject sub = cut.getSubjectBySessionId(id);
        assertThat(sub).isNotNull();
    }

    @Test
    void throwsAnExceptionIfSubjectTriesToAttachToNonexistentSession() throws Exception {
        UUID id = UUID.randomUUID();
        assertThat(cut.sessionExists(id)).isFalse();
        assertThatThrownBy(() -> {
            cut.attachSubjectToSession(id, TestDataCreators.createTestSubject());
        }).isInstanceOf(SessionNotExistsException.class).hasMessage("Pending session [id=" + id + "] does not exist.");
    }
}
