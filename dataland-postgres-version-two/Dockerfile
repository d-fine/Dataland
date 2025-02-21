# basis image from docker
FROM postgres:17.0@sha256:f176fef320ed02c347e9f85352620945547a9a23038f02b57cf7939a198182ae

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1