package dev.cambriota.identityprovider.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.net.HttpHeaders;
import dev.cambriota.identityprovider.exceptions.SessionNotExistsException;
import dev.cambriota.identityprovider.model.Subject;
import dev.cambriota.identityprovider.service.CredentialSubjectMapper;
import dev.cambriota.identityprovider.service.PresentationVerificationService;
import dev.cambriota.identityprovider.service.SessionManagementService;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.UUID;

import static dev.cambriota.identityprovider.util.ApplicationProfileLoader.isSkipVerification;

/**
 * Creates REST endpoint: POST /auth/realms/iota/authenticate
 *
 * @see <a href="https://github.com/keycloak/keycloak/blob/master/examples/providers/rest/src/main/java/org/keycloak/examples/rest/HelloResourceProvider.java">HelloResourceProvider.java</a>
 */
public class DidRealmResourceProvider implements RealmResourceProvider {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final KeycloakSession session;

    private final static String ALLOWED_ORIGINS = "*";

    public DidRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @OPTIONS
    public Response optionsRequest() {
        log.debugf("Sending OPTIONS ...");
        return Response.ok()
                .header("Access-Control-Allow-Origin", ALLOWED_ORIGINS)
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateWithVerifiablePresentation(String json) {
        log.infof("Received request: body=[%s]", json);

        AuthenticationRequest request;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            request = mapper.readValue(json, AuthenticationRequest.class);
        } catch (JsonMappingException e) {
            log.infof("%s", e.getMessage());
            return Response.status(400).build();
        } catch (JsonProcessingException e) {
            log.debugf("%s", e.getMessage());
            return Response.status(400).build();
        }

        JsonNode verifiablePresentation = request.verifiablePresentation;

        UUID sessionId = UUID.fromString(verifiablePresentation.get("proof").get("challenge").asText());

        SessionManagementService sessionManagement = SessionManagementService.getInstance();

        try {
            Subject sub = sessionManagement.getSubjectBySessionId(sessionId);

            LocalDateTime requestForDeletion = request.getMeta().getRequestDeletionAt();

            boolean isVerified;

            if (isSkipVerification()) {
                log.warn("Skipping credential verification ...");
                isVerified = true;
            } else {
                PresentationVerificationService service = new PresentationVerificationService(session);
                isVerified = service.verifyPresentation(verifiablePresentation);
            }

            if (isVerified) {
                JsonNode credentialSubject = verifiablePresentation
                        .get("verifiableCredential")
                        .get("credentialSubject");
                String holder = verifiablePresentation.get("holder").asText();

                Subject subject = CredentialSubjectMapper.mapClaims(credentialSubject);
                subject.setDid(holder);
                subject.setRequestRemovalAt(requestForDeletion);

                SessionManagementService.getInstance().attachSubjectToSession(sessionId, subject);
                log.infof("Successfully verified credential for holder=[%s]", holder);

                return Response
                        .ok()
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGINS)
                        .entity("{\"message\": \"verification successful\"}")
                        .build();
            } else {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGINS)
                        .entity("{\"message\": \"verification failed\"}")
                        .build();
            }
        } catch (SessionNotExistsException exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGINS)
                    .entity("{\"message\": \"no session for id=[" + sessionId + "]\"}")
                    .build();
        }
    }

    @Override
    public void close() {
    }
}
