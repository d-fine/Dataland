# basis image from docker
FROM postgres:15.12@sha256:8f6fbd24a12304d2adc332a2162ee9ff9d6044045a0b07f94d6e53e73125e11c

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1