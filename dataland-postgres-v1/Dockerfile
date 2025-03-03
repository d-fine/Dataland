# basis image from docker
FROM postgres:15.11@sha256:b3a3b666dfb3a7dca6858ef90bad3557da4d4e1532c090f6fe6d4de76d98ab6f

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1