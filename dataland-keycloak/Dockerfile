FROM quay.io/keycloak/keycloak:latest as builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
#ENV KC_FEATURES=authorization,account2,account-api,admin-fine-grained-authz,admin2,docker,impersonation,openshift-integration,scripts,token-exchange,web-authn,client-policies,ciba,map-storage,par,declarative-user-profile,dynamic-scopes,client-secret-rotation,step-up-authentication,recovery-codes,preview
#ENV KC_FEATURES=admin2
#ENV KC_FEATURES=token-exchange
ENV KC_DB=postgres
# Install custom providers
RUN curl -sL https://github.com/aerogear/keycloak-metrics-spi/releases/download/2.5.3/keycloak-metrics-spi-2.5.3.jar -o /opt/keycloak/providers/keycloak-metrics-spi-2.5.3.jar
RUN /opt/keycloak/bin/kc.sh build

#FROM quay.io/keycloak/keycloak:latest
#COPY --from=builder /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak
# for demonstration purposes only, please make sure to use proper certificates in production instead
#RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
# change these values to point to a running postgres instance
ENV KC_DB_URL=jdbc:postgresql://keycloak-db/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_DB_PASSWORD=password
#ENV KC_HOSTNAME=localhost:8080
ENV KC_HOSTNAME_STRICT=false
ENV KC_HOSTNAME_STRICT_HTTPS=false
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY=edge
#ENV KC_SPI_STICKY_SESSION_ENCODER_INFINISPAN_SHOULD_ATTACH_ROUTE=false
#ENV KC_PROXY_ADDRESS_FORWARDING=true
#ENV KC_LOG_LEVEL=debug
#RUN /opt/keycloak/bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]
