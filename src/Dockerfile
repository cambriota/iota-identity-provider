FROM jboss/keycloak:16.1.0

#ENV KEYCLOAK_VERSION 15.0.2
ENV SPI_VERSION 0.1.0
ENV KEYCLOAK_USER admin
ENV KEYCLOAK_PASSWORD admin
ENV KEYCLOAK_IMPORT /opt/jboss/keycloak/realm-iota.json

COPY ./build/libs/iota-identity-keycloak-spi-${SPI_VERSION}.jar /opt/jboss/keycloak/standalone/deployments/
#COPY ./example-realm.json /opt/jboss/keycloak/
# COPY ./build/libs/keycloak-metrics-spi-2.4.1-SNAPSHOT.jar /opt/jboss/keycloak/standalone/deployments
#COPY ./build/libs/keycloak-user-store-1.0.0-SNAPSHOT.jar /opt/jboss/keycloak/standalone/deployments/

# TODO: release: refactor init realm
COPY ../realm-iota.json /opt/jboss/keycloak

# cp keycloak-metrics-spi-2.4.0.jar /opt/bitnami/keycloak/standalone/deployments
# chmod 755 metrics/keycloak-metrics-spi-2.4.0.jar
# touch metrics/keycloak-metrics-spi-2.4.0.jar.dodeploy

# COPY ./src/main/theme/material /opt/jboss/keycloak/themes/material
# TODO: release: do not package keywind into spi
COPY ../theme/keywind /opt/jboss/keycloak/themes/keywind

#VOLUME ["./example-realm.json"]
