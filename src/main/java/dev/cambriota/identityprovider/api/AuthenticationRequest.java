package dev.cambriota.identityprovider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthenticationRequest {
    MetaData meta;

    @JsonProperty("presentation")
    JsonNode verifiablePresentation;

    @Data
    static class MetaData {
        LocalDateTime requestDeletionAt; // "2022-03-14T20:21:43.928Z"
        LocalDateTime requestAnonymizationAt; // "2022-03-14T20:21:43.928Z"
    }
}
