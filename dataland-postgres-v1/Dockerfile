# basis image from docker
FROM postgres:15.15@sha256:98fe06b500b5eb29e45bf8c073eb0ca399790ce17b1d586448edc4203627d342

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1