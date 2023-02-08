#!/bin/bash
set -euxo pipefail
# Ensure /certs director is present and permissions are configured accordingly
echo Creating Certificate Reload Hook
mkdir -p /etc/letsencrypt/renewal-hooks/deploy/
ln -sf /scripts/reload-certificates.sh /etc/letsencrypt/renewal-hooks/deploy/reload-certificates.sh
