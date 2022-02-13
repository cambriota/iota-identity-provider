package dev.cambriota.identityprovider.api;

import dev.cambriota.identityprovider.KeycloakTestBase;
import dev.cambriota.identityprovider.TestDataCreators;
import dev.cambriota.identityprovider.model.Subject;
import dev.cambriota.identityprovider.service.SessionManagementService;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DidRealmResourceProviderTest extends KeycloakTestBase {
    SessionManagementService sessionManagement = SessionManagementService.getInstance();

    DidRealmResourceProvider cut = new DidRealmResourceProvider(session);

    @Test
    void optionsAreSetCorrectly() {
        Response res = cut.optionsRequest();

        Map<String, List<Object>> expectedHeaders = Map.ofEntries(
                new AbstractMap.SimpleEntry<>("Access-Control-Allow-Origin", List.of("*")),
                new AbstractMap.SimpleEntry<>("Access-Control-Allow-Headers", List.of("origin, content-type, accept, authorization"))
        );

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getHeaders()).containsAllEntriesOf(expectedHeaders);
    }

    @Test
    void credentialSubjectIsMappedCorrectly() throws Exception {
        AuthenticationRequest request = TestDataCreators.createTestAuthenticationRequest();
        String sessionId = "fc2cef1d-8e21-4284-a5a7-9ab44ad25c6b";

        sessionManagement.createNewSessionForId(sessionId);
        sessionManagement.attachSubjectToSession(UUID.fromString(sessionId), null);

        Response res = cut.authenticateWithVerifiablePresentation(request);

        Subject expected = new Subject(
                null,
                "jane.doe",
                "Jane",
                "Doe",
                "did:iota:kv2fTV5BYBSpAMwvFN3b1z8iqcTk7ZHNa9AMeGY6pP6",
                LocalDateTime.parse("2022-02-13T17:25:45.326")
        );

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(sessionManagement.getSubjectBySessionId(UUID.fromString(sessionId))).isEqualTo(expected);
    }
}
