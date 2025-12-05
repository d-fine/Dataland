#!/bin/sh

if [ "$DISABLE_HSTS" = "true" ]; then
    export HSTS_VALUE=""
else
    export HSTS_VALUE="max-age=31536000; includeSubDomains"
fi

envsubst '${HSTS_VALUE}' < /etc/nginx/utils/securityHeaderMap.conf.template > /etc/nginx/utils/securityHeaderMap.conf
