FROM dpage/pgadmin4:8.13@sha256:561c1f8f99f2fe24bc63814db9e30292cf4360331de9182da21e3554ce61bdaa

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
