#!/bin/sh

envsubst < /pgpass.template > /var/lib/pgadmin/pgpass

/entrypoint.sh
