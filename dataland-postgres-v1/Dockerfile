# basis image from docker
FROM postgres:18.0@sha256:41fc5342eefba6cc2ccda736aaf034bbbb7c3df0fdb81516eba1ba33f360162c

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1