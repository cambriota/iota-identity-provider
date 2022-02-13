package dev.cambriota.identityprovider.api;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

import java.util.HashMap;
import java.util.Map;

public class DidRealmResourceProviderFactory implements RealmResourceProviderFactory, ServerInfoAwareProviderFactory {

    /* name of endpoint */
    public static final String ID = "authenticate";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new DidRealmResourceProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("docs", "https://github.com/cambriota/iota-identity-provider");
        return info;
    }
}
