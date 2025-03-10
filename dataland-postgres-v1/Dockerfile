# basis image from docker
FROM postgres:15.12@sha256:d5f1596ddc41fd01ba0063b798c95431f8468aea4c04b4140f2574e85a631627

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1