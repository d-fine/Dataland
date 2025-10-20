# basis image from docker
FROM postgres:15.14@sha256:1cd9dd548427751dc0fabb24a3bdf96a6dd08c0025a167ecbb5c14bec21ff94c

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1