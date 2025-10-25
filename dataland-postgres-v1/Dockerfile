# basis image from docker
FROM postgres:15.14@sha256:424e79b81868f5fc5cf515eaeac69d288692ebcca7db86d98f91b50d4bce64bb

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1