# basis image from docker
FROM postgres:15.14@sha256:bc51cf4f1fe02cce7ed2370b20128a9b00b4eb804573a77d2a0d877aaa9c82b1

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1