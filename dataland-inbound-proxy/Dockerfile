ARG DATALAND_PROXY_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_proxy_base:${DATALAND_PROXY_BASE_VERSION}

ARG PROXY_ENVIRONMENT
COPY dataland-inbound-proxy/templates/*-dataland-$PROXY_ENVIRONMENT.template /etc/nginx/templates/
COPY ./gitinfo /var/www/html/gitinfo
