package dev.cambriota.identityprovider.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.cambriota.identityprovider.KeycloakTestBase;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PresentationVerificationServiceTest extends KeycloakTestBase {

    PresentationVerificationService cut = new PresentationVerificationService(session);

    static WireMockServer wireMockServer;

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(7001);
        wireMockServer.start();
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void returnTrueIfAllPartsAreVerified() {
        when (httpClientProvider.getHttpClient()).thenReturn(HttpClients.createDefault());

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("verification-result.json");
        JsonObject verificationResult = Json.createReader(is).readObject();

        configureFor(7001);
        stubFor(post(urlPathEqualTo("/verify")).willReturn(aResponse().withBody(verificationResult.toString())));

        boolean actual = cut.verifyPresentation(verificationResult);

        assertThat(actual).isTrue();
    }
}
