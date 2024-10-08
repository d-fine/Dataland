ARG DATALAND_GRADLE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_gradle_base:${DATALAND_GRADLE_BASE_VERSION} AS builder

WORKDIR /app
COPY ./dataland-keycloak/build.gradle.kts ./dataland-keycloak/
COPY ./dataland-keycloak/dataland_theme/login ./dataland-keycloak/dataland_theme/login
RUN --mount=type=secret,id=DOCKER_SECRET,target=/root/.gradle/gradle.properties,required=true \
     ./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace

FROM registry.access.redhat.com/ubi9@sha256:7575b6e3cc492f856daf8c43f30692d8f5fcd5b7077806dba4bac436ad0a84e8 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs curl --releasever 9 --setopt install_weak_deps=false --nodocs -y; dnf --installroot /mnt/rootfs clean all

FROM quay.io/keycloak/keycloak:24.0.4@sha256:ff02c932f0249c58f32b8ff1b188a48cc90809779a3a05931ab67f5672400ad0

COPY --from=ubi-micro-build /mnt/rootfs /

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
COPY --from=builder /app/dataland-keycloak/dataland_theme/login/build/dist /opt/keycloak/themes/dataland_theme/login
COPY ./dataland-keycloak/dataland_theme/email /opt/keycloak/themes/dataland_theme/email
COPY ./dataland-keycloak/start_keycloak.sh /opt/keycloak/bin
COPY ./dataland-keycloak/realms /keycloak_realms

RUN /opt/keycloak/bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/start_keycloak.sh"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/keycloak/realms/datalandsecurity/ | tee /dev/stderr | grep -q 'datalandsecurity' || false
