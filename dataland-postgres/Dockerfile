# basis image from docker
FROM postgres:18.4@sha256:1a5b3e745bbd82d6deb146505e504da3c2f248cac15e431951b148fbe4f8613a

HEALTHCHECK --interval=30s --timeout=3s CMD pg_isready || false || exit 1