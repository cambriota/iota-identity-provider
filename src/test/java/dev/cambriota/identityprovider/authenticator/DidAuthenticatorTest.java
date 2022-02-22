package dev.cambriota.identityprovider.authenticator;

import dev.cambriota.identityprovider.TestDataCreators;
import dev.cambriota.identityprovider.model.Subject;
import dev.cambriota.identityprovider.service.SessionManagementService;
import dev.cambriota.identityprovider.util.QrCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.UUID;

import static org.mockito.Mockito.*;

class DidAuthenticatorTest {

    AuthenticationFlowContext context = mock(AuthenticationFlowContext.class, RETURNS_DEEP_STUBS);
    LoginFormsProvider forms = mock(LoginFormsProvider.class);
    JsonObjectBuilder objectBuilder = mock(JsonObjectBuilder.class);

    DidAuthenticator cut = new DidAuthenticator();

    @BeforeEach
    void setUp() {
        String parentSessionId = UUID.randomUUID().toString();
        String tabId = "abCdEf123";

        when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);
        when(context.getAuthenticationSession().getTabId()).thenReturn(tabId);
    }

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTest {

        @Test
        void shouldPresentNewLoginSession_AndQrCodeShouldBeCalled() {
            String parentSessionId = UUID.randomUUID().toString();
            String tabId = "abCdEf123";

            when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);
            when(context.getAuthenticationSession().getTabId()).thenReturn(tabId);

            when(forms.setAttribute(anyString(), any())).thenReturn(forms);
            when(context.form()).thenReturn(forms);

            try (MockedStatic<QrCode> qrCode = Mockito.mockStatic(QrCode.class)) {
                qrCode.when(() -> QrCode.generateQRCodeImage(anyString())).thenReturn("qrCodeImageAsString");

                cut.authenticate(context);

                verify(forms, times(3)).setAttribute(anyString(), anyString());
                verify(forms).setAttribute("endpoint", "http://localhost:8080/auth/realms/iota/authenticate");
                verify(forms).setAttribute("id", parentSessionId);
                verify(forms).setAttribute("qrcode", "qrCodeImageAsString");
                verify(forms).createForm("did-authenticate.ftl");
            }
        }

        @Test
        void correctJsonForQrCodeIsBuilt() {
            String parentSessionId = UUID.randomUUID().toString();
            String tabId = "abCdEf123";

            when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);
            when(context.getAuthenticationSession().getTabId()).thenReturn(tabId);

            // create JsonObject for mock
            JsonReader jsonReader = Json.createReader(new StringReader("{\"key\": \"value\"}"));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            when(objectBuilder.add(anyString(), anyString())).thenReturn(objectBuilder);
            when(objectBuilder.build()).thenReturn(jsonObject);

            try (MockedStatic<Json> json = Mockito.mockStatic(Json.class)) {
                json.when(Json::createObjectBuilder).thenReturn(objectBuilder);

                cut.authenticate(context);

                verify(objectBuilder, times(3)).add(anyString(), anyString());
                verify(objectBuilder).add("type", "LoginWithIOTAIdentity");
                verify(objectBuilder).add("url", "http://localhost:8080/auth/realms/iota/authenticate");
                verify(objectBuilder).add("challenge", parentSessionId);
                verify(objectBuilder).build();
            }
        }
    }

    @Nested
    @DisplayName("action")
    class ActionTest {

        @Test
        void shouldCreateNewUser_WhenSubjectIsPresent_AndUserNotExists() throws Exception {
            String parentSessionId = UUID.randomUUID().toString();
            Subject sub = TestDataCreators.createTestSubject();
            UserModel user = mock(UserModel.class);

            SessionManagementService sessionManagement = SessionManagementService.getInstance();
            sessionManagement.createNewSessionForId(parentSessionId);
            sessionManagement.attachSubjectToSession(UUID.fromString(parentSessionId), sub);

            when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);
            when(context.getSession().users().getUserByUsername(any(), anyString())).thenReturn(null);
            when(context.getSession().users().addUser(any(), anyString())).thenReturn(user);

            cut.action(context);

            verify(context.getSession().users()).addUser(context.getRealm(), sub.getUsername());
            verify(user).setEmail(sub.getEmail());
            verify(user).setFirstName(sub.getFirstName());
            verify(user).setLastName(sub.getLastName());
            verify(user).setEnabled(true);
            verify(user).setSingleAttribute("did", sub.getDid());
            verify(user).setSingleAttribute("requestDeletionAt", sub.getRequestDeletionAt().toString());
            verify(context).setUser(any(UserModel.class));
            verify(context).success();
        }

        @Test
        void shouldOverwriteUserClaims_WhenSubjectIsPresent_AndUserWithDidAlreadyExists() throws Exception {
            String parentSessionId = UUID.randomUUID().toString();
            String did = TestDataCreators.createTestDid();
            Subject sub = TestDataCreators.createTestSubject();
            sub.setFirstName("Bruce");
            sub.setDid(did);
            UserModel user = mock(UserModel.class, RETURNS_DEEP_STUBS);

            SessionManagementService sessionManagement = SessionManagementService.getInstance();
            sessionManagement.createNewSessionForId(parentSessionId);
            sessionManagement.attachSubjectToSession(UUID.fromString(parentSessionId), sub);

            when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);
            when(context.getSession().users().getUserByUsername(any(), anyString())).thenReturn(user);
            when(user.getUsername()).thenReturn(sub.getUsername());
            when(user.getAttributes().get("did").get(0)).thenReturn(did);

            cut.action(context);

            verify(context.getSession().users(), never()).addUser(any(), anyString());
            verify(user).setEmail(sub.getEmail());
            verify(user).setFirstName(sub.getFirstName());
            verify(user).setLastName(sub.getLastName());
            verify(user).setEnabled(true);
            verify(user).setSingleAttribute("did", sub.getDid());
            verify(user).setSingleAttribute("requestDeletionAt", sub.getRequestDeletionAt().toString());
            verify(context).setUser(any(UserModel.class));
            verify(context).success();
        }

        @Test
        void shouldThrowError_WhenSubjectIsPresent_AndUserWithDidAlreadyExists_AndSubHasDifferentDid() throws Exception {
            String parentSessionId = UUID.randomUUID().toString();
            String did = TestDataCreators.createTestDid();
            String modifiedDid = did.substring(0, did.length() - 3) + "xyz"; // TODO: replace with proper test generator
            Subject sub = TestDataCreators.createTestSubject();
            sub.setFirstName("Bruce");
            sub.setDid(modifiedDid);
            UserModel user = mock(UserModel.class, RETURNS_DEEP_STUBS);

            SessionManagementService sessionManagement = SessionManagementService.getInstance();
            sessionManagement.createNewSessionForId(parentSessionId);
            sessionManagement.attachSubjectToSession(UUID.fromString(parentSessionId), sub);

            when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);
            when(context.getSession().users().getUserByUsername(any(), anyString())).thenReturn(user);
            when(user.getUsername()).thenReturn(sub.getUsername());
            when(user.getAttributes().get("did").get(0)).thenReturn(did);

            cut.action(context);

            verify(context.getSession().users(), never()).addUser(any(), anyString());
            verify(context).failureChallenge(eq(AuthenticationFlowError.USER_CONFLICT), any());
            verify(context, never()).success();
        }

        @Test
        void shouldShowErrorMessage_WhenNoSubjectPresent() {
            String parentSessionId = UUID.randomUUID().toString();

            SessionManagementService.getInstance().createNewSessionForId(parentSessionId);

            when(context.getAuthenticationSession().getParentSession().getId()).thenReturn(parentSessionId);

            when(forms.setAttribute(anyString(), any())).thenReturn(forms);
            when(forms.setError(anyString(), any())).thenReturn(forms);
            when(context.form()).thenReturn(forms);

            try (MockedStatic<QrCode> qrCode = Mockito.mockStatic(QrCode.class)) {
                qrCode.when(() -> QrCode.generateQRCodeImage(anyString())).thenReturn("qrCodeImageAsString");

                cut.action(context);

                verify(forms, times(3)).setAttribute(anyString(), anyString());
                verify(forms).setAttribute("endpoint", "http://localhost:8080/auth/realms/iota/authenticate");
                verify(forms).setAttribute("id", parentSessionId);
                verify(forms).setAttribute("qrcode", "qrCodeImageAsString");
                verify(forms).setError("No session found. Please scan the QR Code.");
                verify(forms).createForm("did-authenticate.ftl");
                verify(context).failureChallenge(eq(AuthenticationFlowError.UNKNOWN_USER), any());
                verify(context, never()).success();
            }
        }
    }
}
