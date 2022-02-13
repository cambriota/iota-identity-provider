package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.TestDataCreators;
import dev.cambriota.identityprovider.model.Subject;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

        JsonObject given = Json.createObjectBuilder()
                .add("firstName", "John")
                .add("username", "johndoe")
                .build();

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

        JsonObject given = Json.createObjectBuilder()
                .add("firstName", "John")
                .add("lastName", "Doe")
                .add("myKey", "myValue")
                .build();

        Subject actual = CredentialSubjectMapper.mapClaims(given);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createsRandomAlphanumericUsernameIfNoInformationGiven() {
        JsonObject given = Json.createObjectBuilder()
                .build();

        Subject actual = CredentialSubjectMapper.mapClaims(given);

        assertThat(actual.getUsername().length()).isEqualTo(16);
    }
}
