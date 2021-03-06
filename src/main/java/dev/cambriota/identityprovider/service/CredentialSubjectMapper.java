package dev.cambriota.identityprovider.service;

import dev.cambriota.identityprovider.model.Subject;
import org.apache.commons.lang.RandomStringUtils;

import javax.json.JsonObject;

public class CredentialSubjectMapper {

    public static Subject mapClaims(JsonObject credentialSubject) {

        String email = getProperty(credentialSubject, "email");
        String username = getProperty(credentialSubject, "username");
        String firstName = getProperty(credentialSubject, "firstName");
        String lastName = getProperty(credentialSubject, "lastName");

        return new Subject(
                email,
                (username != null) ? username : constructUsername(firstName, lastName),
                firstName,
                lastName,
                null,
                null
        );
    }

    private static String getProperty(JsonObject json, String name) {
        try {
            return json.getString(name);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static String constructUsername(String firstName, String lastName) {
        if (firstName == null && lastName == null) return RandomStringUtils.randomAlphanumeric(16);
        if (firstName == null) return lastName.toLowerCase();
        if (lastName == null) return firstName.toLowerCase();
        return firstName.toLowerCase() + "." + lastName.toLowerCase();
    }
}
