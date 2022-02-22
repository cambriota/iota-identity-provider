package dev.cambriota.identityprovider.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@ToString
public class Subject {
    String email;
    String username;
    String firstName;
    String lastName;
    String did;
    LocalDateTime requestDeletionAt;
}
