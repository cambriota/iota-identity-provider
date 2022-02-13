package dev.cambriota.identityprovider.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.JsonObject;
import java.time.LocalDateTime;

@Data
public class AuthenticationRequest {
    MetaData meta;
    JsonObject credential; // VerifiablePresentation

    @Data
    @AllArgsConstructor
    public class MetaData {
        LocalDateTime requestForDeletion; // "2022-03-14T20:21:43.928Z"
        LocalDateTime requestForAnonymization; // "2022-03-14T20:21:43.928Z"
    }
}
