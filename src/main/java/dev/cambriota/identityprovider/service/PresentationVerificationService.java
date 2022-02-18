package dev.cambriota.identityprovider.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static dev.cambriota.identityprovider.util.ApplicationProfileLoader.getSidecarUrl;

/**
 * Calls a small "sidecar" service (written in Node.js, contains @iota/identity-wasm) which performs the
 * actual verification with the Tangle.
 */
public class PresentationVerificationService {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final KeycloakSession session;

    private static final String SIDECAR_URL = getSidecarUrl();

    public PresentationVerificationService(KeycloakSession session) {
        this.session = session;
    }

    public boolean verifyPresentation(JsonNode verifiablePresentation) {
        log.infof("Verifying credential from holder=[%s] ...", verifiablePresentation.get("holder"));
        try {
            HttpClient client = session.getProvider(HttpClientProvider.class).getHttpClient();

            // request
            HttpPost req = new HttpPost(SIDECAR_URL + "/verify");
            StringEntity requestEntity = new StringEntity(verifiablePresentation.toString(), ContentType.APPLICATION_JSON);
            req.setEntity(requestEntity);

            // response
            HttpResponse res = client.execute(req);
            String body = EntityUtils.toString(res.getEntity());
            JsonReader reader = Json.createReader(new StringReader(body));
            JsonObject verificationResult = reader.readObject();
            reader.close();

            Boolean holderVerified = verificationResult
                    .getJsonObject("holder")
                    .getBoolean("verified");

            Boolean issuerVerified = verificationResult
                    .getJsonArray("credentials")
                    .getJsonObject(0)
                    .getJsonObject("issuer")
                    .getBoolean("verified");

            Boolean credentialsVerified = verificationResult
                    .getJsonArray("credentials")
                    .getJsonObject(0)
                    .getBoolean("verified");

            Boolean totalVerified = verificationResult
                    .getBoolean("verified");

            return holderVerified && issuerVerified && credentialsVerified && totalVerified;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
