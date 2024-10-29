FROM dpage/pgadmin4:8.12@sha256:585350593e8b0483941fa9a2d5ebb04b88a66e83a7b5603c169f9446b271312f

USER root

COPY ./dataland-pgadmin/custom_entrypoint.sh /custom_entrypoint.sh
COPY ./dataland-pgadmin/pgpass.template /pgpass.template
COPY ./dataland-pgadmin/servers.json /pgadmin4/servers.json

RUN touch /var/lib/pgadmin/pgpass && chown 5050:5050 /var/lib/pgadmin/pgpass && chmod 0600 /var/lib/pgadmin/pgpass && chmod a+rx /custom_entrypoint.sh

# Install envsubst
RUN apk add --no-cache gettext

USER pgadmin
ENTRYPOINT ["/custom_entrypoint.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=120s CMD wget -nv -O- -t 1 http://localhost/pgadmin/misc/ping | tee /dev/stderr | grep -q PING || false
