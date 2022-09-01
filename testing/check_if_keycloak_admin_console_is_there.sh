#!/bin/bash
# This script verifies that the keycloak admin console can be accessed from localhost

set -ex

if ! curl -L http://localhost:6789/keycloak/admin | grep -q 'Keycloak Administration Console'; then
  echo "Keycloak admin console was not found"
  exit 1
else
  echo "Keycloak admin console was found"
  exit 0
fi
