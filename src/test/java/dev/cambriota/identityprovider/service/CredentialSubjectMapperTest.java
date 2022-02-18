package dev.cambriota.identityprovider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.cambriota.identityprovider.model.Subject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialSubjectMapperTest {

    @Test
    void mapsClaimsCorrectly() {
        Subject expected = new Subject(
                null,
                "johndoe",
                "John",
                null,
                null,
                null
        );

        JsonNode given = JsonNodeFactory.instance.objectNode();
        ((ObjectNode) given).put("firstName", "John");
        ((ObjectNode) given).put("username", "johndoe");

        Subject actual = CredentialSubjectMapper.mapClaims(given);

        assertThat(actual.equals(expected)).isTrue();
    }

    @Test
    void ignoresUnknownClaims() {
        Subject expected = new Subject(
                null,
                "john.doe",
                "John",
                "Doe",
                null,
                null
        );

        JsonNode given = JsonNodeFactory.instance.objectNode();
        ((ObjectNode) given).put("firstName", "John");
        ((ObjectNode) given).put("lastName", "Doe");
        ((ObjectNode) given).put("myKey", "myValue");

        Subject actual = CredentialSubjectMapper.mapClaims(given);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createsRandomAlphanumericUsernameIfNoInformationGiven() {
        JsonNode given = JsonNodeFactory.instance.objectNode();

        Subject actual = CredentialSubjectMapper.mapClaims(given);

        assertThat(actual.getUsername().length()).isEqualTo(16);
    }
}
