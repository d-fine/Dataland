FROM quay.io/keycloak/keycloak:20.0.1

ENV KC_DB=postgres
ENV KC_DB_URL=jdbc:postgresql://keycloak-db/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_HOSTNAME_STRICT=false
ENV KC_HOSTNAME_STRICT_HTTPS=false
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY_ADDRESS_FORWARDING=true
COPY ./dataland-keycloak/dataland_theme/login/dist /opt/keycloak/themes/dataland_theme/login
COPY ./dataland-keycloak/dataland_theme/email /opt/keycloak/themes/dataland_theme/email
ENV KC_PROXY=edge
ENV KC_HTTP_RELATIVE_PATH=keycloak
ENV KC_HOSTNAME_ADMIN=dataland-admin:6789
COPY ./dataland-keycloak/start_keycloak.sh /opt/keycloak/bin

RUN /opt/keycloak/bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/start_keycloak.sh"]
