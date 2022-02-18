package dev.cambriota.identityprovider.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        JsonNode requestAsJson = TestDataCreators.getResourceAsJsonNode("authentication-request.json");
        String sessionId = requestAsJson.get("presentation").get("proof").get("challenge").asText();

        sessionManagement.createNewSessionForId(sessionId);
        sessionManagement.attachSubjectToSession(UUID.fromString(sessionId), null);

        Response res = cut.authenticateWithVerifiablePresentation(requestAsJson.toString());

        Subject expected = new Subject(
                null,
                "jane.doe",
                "Jane",
                "Doe",
                "did:iota:kv2fTV5BYBSpAMwvFN3b1z8iqcTk7ZHNa9AMeGY6pP6",
                LocalDateTime.parse("2022-03-14T20:21:43.928")
        );

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(sessionManagement.getSubjectBySessionId(UUID.fromString(sessionId))).isEqualTo(expected);
    }

    @Test
    void returns400_WhenRequestCanNotBeParsed() {
        JsonNode requestAsJson = TestDataCreators.getResourceAsJsonNode("authentication-request.json");
        String brokenRequest = requestAsJson.toString().replace(":", "§");

        Response res = cut.authenticateWithVerifiablePresentation(brokenRequest);

        assertThat(res.getStatus()).isEqualTo(400);
        assertThat(res.getEntity()).isNull();
    }

    @Test
    void returns400_WhenRequestCanNotBeMappedToObject() {
        JsonNode requestAsJson = TestDataCreators.getResourceAsJsonNode("authentication-request.json");

        JsonNode invalidMetadata = JsonNodeFactory.instance.objectNode();
        ((ObjectNode) invalidMetadata).put("unknownKey", "some-value");

        ObjectNode modified = (ObjectNode) requestAsJson;
        modified.replace("meta", invalidMetadata);

        Response res = cut.authenticateWithVerifiablePresentation(modified.toString());

        assertThat(res.getStatus()).isEqualTo(400);
        assertThat(res.getEntity()).isNull();
    }
}
