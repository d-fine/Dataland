# basis image from docker
FROM postgres:15.12@sha256:fe45ed1a824b81c0c9c605890963b67501758ca8c946db89089c85ce0f88e974

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1