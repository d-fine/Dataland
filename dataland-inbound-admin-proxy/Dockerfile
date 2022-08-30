FROM nginx
ARG PROXY_ENVIRONMENT

COPY config /etc/nginx
COPY nginx-$PROXY_ENVIRONMENT.conf /etc/nginx/nginx.conf