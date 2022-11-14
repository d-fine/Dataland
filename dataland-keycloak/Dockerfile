FROM eclipse-temurin:17.0.5_8-jdk-jammy as builder
ARG GITHUB_USER
ARG GITHUB_TOKEN

COPY ./gradle/ /app/gradle/
COPY ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./gradlew /app/
COPY ./dataland-keycloak/build.gradle.kts /app/dataland-keycloak/
COPY ./dataland-keycloak/dataland_theme/login /app/dataland-keycloak/dataland_theme/login
WORKDIR /app
RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN ./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace

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
