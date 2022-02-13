FROM busybox
ENV IOTA_IDENTITY_SPI_VERSION=0.1.0

ADD ./build/libs/iota-identity-keycloak-spi-${IOTA_IDENTITY_SPI_VERSION}.jar \
    /iota-identity-spi/
