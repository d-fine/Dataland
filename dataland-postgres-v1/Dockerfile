# basis image from docker
FROM postgres:15.15@sha256:a7711af921c380de0fda7c48a61fab5ac4def145fd9fc00ccea7355fd6270bb4

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1