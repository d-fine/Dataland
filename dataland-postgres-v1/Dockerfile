# basis image from docker
FROM postgres:15.14@sha256:822f8795764a670160640888508b2a68ea5c4b045012c2de17e1d0447bdbdc99

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1