# basis image from docker
FROM postgres:15.13@sha256:5ab68e212eab9cd4a16ecbf40d9449c88e1073abdca3ecc3aa5514d4a1af2ed0

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1