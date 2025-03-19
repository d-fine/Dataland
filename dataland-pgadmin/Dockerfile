FROM dpage/pgadmin4:9.1@sha256:bdebdfc4b165c10d0ad60e58f1d7ef41af6c881c9556ae331adaa35bba6dacf3

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
