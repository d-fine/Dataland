# basis image from docker
FROM postgres:17.6@sha256:00bc86618629af00d2937fdc5a5d63db3ff8450acf52f0636ec813c7f4902929

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1