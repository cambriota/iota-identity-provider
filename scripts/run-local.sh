#!/bin/sh
VERSION="0.1.0"

./gradlew jar

docker run --rm --name keycloak -p 8181:8080 -p 8787:8787 \
  -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin \
  -e KEYCLOAK_IMPORT=/tmp/realm-iota.json \
  -e JAVA_OPTS_APPEND="-Dprofile=local" \
  -v $(pwd)/../src/main/startup-scripts:/opt/jboss/startup-scripts \
  -v $(pwd)/../realm-iota.json:/tmp/realm-iota.json \
  -v $(pwd)/../build/libs/iota-identity-provider-$VERSION.jar:/opt/jboss/keycloak/standalone/deployments/iota-identity-keycloak-spi-$VERSION.jar \
  -v $(pwd)/../theme/keywind:/opt/jboss/keycloak/themes/keywind \
  jboss/keycloak:16.1.0 \
  --debug *:8787
