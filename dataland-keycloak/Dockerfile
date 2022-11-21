ARG DATALAND_GRADLE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_gradle_base:${DATALAND_GRADLE_BASE_VERSION} as builder

WORKDIR /app
COPY ./dataland-keycloak/build.gradle.kts ./dataland-keycloak/
COPY ./dataland-keycloak/dataland_theme/login ./dataland-keycloak/dataland_theme/login
RUN --mount=type=secret,id=DOCKER_SECRET,target=/root/.gradle/gradle.properties,required=true \
     ./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace

FROM quay.io/keycloak/keycloak:20.0.1

ENV KC_DB=postgres
ENV KC_DB_URL=jdbc:postgresql://keycloak-db/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_HOSTNAME_STRICT=false
ENV KC_HOSTNAME_STRICT_HTTPS=false
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY_ADDRESS_FORWARDING=true
ENV KC_PROXY=edge
ENV KC_HTTP_RELATIVE_PATH=keycloak
ENV KC_HOSTNAME_ADMIN=dataland-admin:6789
COPY --from=builder /app/dataland-keycloak/dataland_theme/login/dist /opt/keycloak/themes/dataland_theme/login
COPY ./dataland-keycloak/dataland_theme/email /opt/keycloak/themes/dataland_theme/email
COPY ./dataland-keycloak/start_keycloak.sh /opt/keycloak/bin
COPY ./dataland-keycloak/realms /keycloak_realms

RUN /opt/keycloak/bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/start_keycloak.sh"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/keycloak/realms/datalandsecurity/ | grep -q 'datalandsecurity'
