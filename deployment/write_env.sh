#!/bin/bash

env_file="./env_variables.sh"
echo "#!/bin/bash" > "$env_file"

variables="EDC_API_AUTH_KEY EDC_API_CONTROL_AUTH_APIKEY_VALUE EDC_KEYSTORE_PASSWORD EDC_OAUTH_PROVIDER_AUDIENCE"
variables+=" EDC_OAUTH_PROVIDER_JWKS_URL EDC_OAUTH_TOKEN_URL EDC_SERVER_URI IDS_WEBHOOK_ADDRESS TRUSTEE_CREDENTIALS"
variables+=" TRUSTEE_IDS_URI TRUSTEE_URI SKYMINDER_URL SKYMINDER_PW SKYMINDER_USER"

for variable in $variables
do
  echo "export $variable=${!variable}" >> "$env_file"
done

echo "export BACKEND_DOCKERFILE=DockerfileBackend" >> "$env_file"
echo "export FRONTEND_DOCKERFILE=DockerfileFrontend" >> "$env_file"
