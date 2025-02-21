# basis image from docker
FROM postgres:15.10@sha256:d609c3005478af92bddad773423df829b7402ea0b356d5b72edd2fd54d1ad3ea

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1