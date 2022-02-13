#!/bin/sh
VERSION="0.1.0"

./gradlew jar

docker build . -t keycloak-iota-identity-spi:$VERSION
