FROM dpage/pgadmin4

# Install envsubst
USER root
RUN apk add gettext && touch /var/lib/pgadmin/pgpass && chown 5050:5050 /var/lib/pgadmin/pgpass && chmod 0600 /var/lib/pgadmin/pgpass
COPY custom_entrypoint.sh /custom_entrypoint.sh

USER pgadmin
ENTRYPOINT ["/custom_entrypoint.sh"]
