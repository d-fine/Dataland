# basis image from docker
FROM postgres:15.12@sha256:9e9298817d19f4bd60c5028a25762b394db37dda173dd3d035a1bc155542051a

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1