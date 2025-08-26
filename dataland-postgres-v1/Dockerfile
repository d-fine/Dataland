# basis image from docker
FROM postgres:15.13@sha256:15d29ec7af31e0f60c2394ed5e3b4d45b6b8d78ffc6faf994e9796c1415b8785

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1