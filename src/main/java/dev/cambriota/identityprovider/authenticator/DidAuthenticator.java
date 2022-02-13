package dev.cambriota.identityprovider.authenticator;

import dev.cambriota.identityprovider.exceptions.SessionNotExistsException;
import dev.cambriota.identityprovider.util.ApplicationProfileLoader;
import dev.cambriota.identityprovider.util.QrCode;
import dev.cambriota.identityprovider.model.Subject;
import dev.cambriota.identityprovider.service.SessionManagementService;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import java.util.UUID;

public class DidAuthenticator implements Authenticator {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private static final String AUTHENTICATION_ENDPOINT = ApplicationProfileLoader.getAuthenticationEndpoint();

    private static final SessionManagementService sessionManagement = SessionManagementService.getInstance();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String parentSessionId = context.getAuthenticationSession().getParentSession().getId();
        String tabId = context.getAuthenticationSession().getTabId();
        log.infof("authenticate: session[parentSessionId=%s, tabId=%s]", parentSessionId, tabId);

        String id = sessionManagement.createNewSessionForId(parentSessionId);

        JsonObject qrCodeValueAsJson = Json.createObjectBuilder()
                .add("type", "LoginWithIOTAIdentity")
                .add("url", AUTHENTICATION_ENDPOINT)
                .add("challenge", parentSessionId)
//                .add("client", "") // TODO: add info about client that wants to request your login data
                .build();

        String qrCode = QrCode.generateQRCodeImage(qrCodeValueAsJson.toString());

        Response challenge = context.form()
                .setAttribute("endpoint", AUTHENTICATION_ENDPOINT)
                .setAttribute("id", parentSessionId)
                .setAttribute("qrcode", qrCode)
                .createForm("did-authenticate.ftl");

        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String parentSessionId = context.getAuthenticationSession().getParentSession().getId();
        String tabId = context.getAuthenticationSession().getTabId();
        log.infof("action: session[parentSessionId=%s, tabId=%s]", parentSessionId, tabId);

        try {
            Subject sub = sessionManagement.getSubjectBySessionId(UUID.fromString(parentSessionId));

            boolean validated;

            if (sub == null) {
                log.infof("No validated DID found for session[id=%s]", parentSessionId);
                validated = false;
            } else {
                log.infof("Found validated DID for session[id=%s]", parentSessionId);
                validated = true;

                log.infof("Trying to populate context with existing user from database ...");

                UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), sub.getUsername());

                if (user == null) {
                    log.infof("User [%s] not found in local database. Creating user ...", sub.getUsername());
                    UserModel created = upsertUser(context, sub);
                    context.setUser(created);
                } else {
                    log.infof("User [%s] found.", user.getUsername());
                    String storedDid = user.getAttributes().get("did").get(0);
                    String providedDid = sub.getDid();
                    if (storedDid.equals(providedDid)) {
                        log.infof("DIDs are matching. Updating user info with new values ...");
                        UserModel updated = upsertUser(context, sub);
                        context.setUser(updated);
                    } else {
                        log.warnf("The stored DID of user=[%s] does not match with the one provided! Rejecting sign in. existing=[%s], provided=[%s]", sub.getUsername(), storedDid, providedDid);
                        Response challenge = context.form()
                                .setAttribute("endpoint", AUTHENTICATION_ENDPOINT)
                                .setAttribute("id", parentSessionId)
                                .setAttribute("qrcode", QrCode.generateQRCodeImage(AUTHENTICATION_ENDPOINT))
                                .setError("User already exists.")
                                .createForm("did-authenticate.ftl");
                        context.failureChallenge(AuthenticationFlowError.USER_CONFLICT, challenge);
                        return;
                    }
                }

                log.infof("User in context: %s", context.getUser().getUsername());
                sessionManagement.destroySession(UUID.fromString(parentSessionId));
            }

            if (!validated) {
                Response challenge = context.form()
                        .setAttribute("endpoint", AUTHENTICATION_ENDPOINT)
                        .setAttribute("id", parentSessionId)
                        .setAttribute("qrcode", QrCode.generateQRCodeImage(AUTHENTICATION_ENDPOINT))
                        .setError("No session found. Please scan the QR Code.")
                        .createForm("did-authenticate.ftl");
                context.failureChallenge(AuthenticationFlowError.UNKNOWN_USER, challenge);
                return;
            }
            context.success();

        } catch (SessionNotExistsException exception) {
            log.infof(exception.getMessage());
        }
    }

    private UserModel upsertUser(AuthenticationFlowContext context, Subject sub) {
        UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), sub.getUsername());
        if (user == null) {
            user = context.getSession().users().addUser(context.getRealm(), sub.getUsername());
        }
        user.setEmail(sub.getEmail());
        user.setFirstName(sub.getFirstName());
        user.setLastName(sub.getLastName());
        user.setEnabled(true);
        user.setSingleAttribute("did", sub.getDid());
        user.setSingleAttribute("requestRemovalAt", sub.getRequestRemovalAt().toString());
        return user;
    }

    @Override
    public boolean requiresUser() {
        log.info("requiresUser: false");
        return false;
    }

    /**
     * Is user configured for this authenticator? --> always true
     */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        log.info("configuredFor()");
//        return getCredentialProvider(session).isConfiguredFor(realm, user, getType(session));
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
