FROM node:18 as builder
WORKDIR /theme
COPY ./dataland-keycloak/dataland_theme/login .
RUN chown -R node:node /theme && chmod +x /theme/build.sh
USER node

RUN npm install && sh ./build.sh

FROM quay.io/keycloak/keycloak:18.0.1

ENV KC_DB=postgres
ENV KC_DB_URL=jdbc:postgresql://keycloak-db/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_HOSTNAME_STRICT=false
ENV KC_HOSTNAME_STRICT_HTTPS=false
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY_ADDRESS_FORWARDING=true
COPY --from=builder /theme/build /opt/keycloak/themes/dataland_theme/login
ENV KC_PROXY=edge
ENV KC_HTTP_RELATIVE_PATH=keycloak

RUN /opt/keycloak/bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]
