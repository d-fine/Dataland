FROM dpage/pgadmin4:6.15

USER root

COPY custom_entrypoint.sh /custom_entrypoint.sh
RUN touch /var/lib/pgadmin/pgpass && chown 5050:5050 /var/lib/pgadmin/pgpass && chmod 0600 /var/lib/pgadmin/pgpass && chmod a+rx /custom_entrypoint.sh

# Install envsubst
RUN apk add gettext

USER pgadmin
ENTRYPOINT ["/custom_entrypoint.sh"]
