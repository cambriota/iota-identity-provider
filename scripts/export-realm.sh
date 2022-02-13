#!/bin/bash

docker exec -it iota-identity-provider_keycloak_1 /opt/jboss/keycloak/bin/standalone.sh \
-Djboss.socket.binding.port-offset=100 \
-Dkeycloak.migration.action=export \
-Dkeycloak.migration.provider=singleFile \
-Dkeycloak.migration.realmName=iota \
-Dkeycloak.migration.usersExportStrategy=REALM_FILE \
-Dkeycloak.migration.file=/tmp/realm-iota.json

docker cp iota-identity-provider_keycloak_1:/tmp/realm-iota.json .
