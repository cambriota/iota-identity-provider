FROM jboss/keycloak:${version.keycloak}

COPY target/classes/startup-scripts/* /opt/jboss/startup-scripts/
COPY target/*.jar /opt/jboss/keycloak/modules/com/github/conciso/keycloak-spi-example/provider/main/
COPY target/classes/module.xml /opt/jboss/keycloak/modules/com/github/conciso/keycloak-spi-example/provider/main/
