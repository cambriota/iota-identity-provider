package dev.cambriota.identityprovider;

import dev.cambriota.identityprovider.api.AuthenticationRequest;
import dev.cambriota.identityprovider.model.Subject;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class TestDataCreators {

    public static Subject createTestSubject() {
        return new Subject(
                "my.name@example.org",
                "johndoe",
                "John",
                "Doe",
                "",
                LocalDateTime.now()
        );
    }

    public static String createTestDid() {
        return "did:iota:kv2fTV5BYBSpAMwvFN3b1z8iqcTk7ZHNa9AMeGY6pP6";
    }

    public static AuthenticationRequest createTestAuthenticationRequest() {

        InputStream is = TestDataCreators.class.getClassLoader().getResourceAsStream("valid-presentation.json");
        JsonObject testCredential = Json.createReader(is).readObject();

        AuthenticationRequest request = new AuthenticationRequest();

        LocalDateTime plusOneHour = LocalDateTime.parse("2022-02-13T17:25:45.326");

        AuthenticationRequest.MetaData metaData = request.new MetaData(plusOneHour, null);
        request.setMeta(metaData);
        request.setCredential(testCredential);

        return request;
    }
}
