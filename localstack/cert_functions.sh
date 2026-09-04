#!/usr/bin/env bash

retrieve_ssl_certificates() {
  mkdir -p ./local/certs

  # When running inside the opencode sandbox, no local SSH agent/key is available.
  # Instead, an SSH private key is provided via the SSL_PRIVATE_KEY secret
  # (see .sandbox/spec.yaml) and used to authenticate against letsencrypt.dataland.com.
  if [ -n "$SSL_PRIVATE_KEY" ]; then
    mkdir -p ~/.ssh
    printf '%s\n' "$SSL_PRIVATE_KEY" > ~/.ssh/letsencrypt_id_rsa
    chmod 600 ~/.ssh/letsencrypt_id_rsa
    scp -i ~/.ssh/letsencrypt_id_rsa -o StrictHostKeyChecking=accept-new \
      ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs
  else
    scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs
  fi
}

generate_self_signed_certificates() {
  mkdir -p ./local/certs
  
  if [ -f ./local/certs/privkey.pem ] && [ -f ./local/certs/fullchain.pem ]; then
    log_info "Self-signed SSL certificates already exist. Skipping generation."
    return 0
  fi
  
  MSYS_NO_PATHCONV=1 openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
    -keyout ./local/certs/privkey.pem \
    -out ./local/certs/fullchain.pem \
    -subj "/C=DE/ST=Hessen/L=Frankfurt/O=DatalandTest/CN=local-dev.dataland.com"
  cp ./local/certs/fullchain.pem ./local/certs/cert.pem
  cp ./local/certs/fullchain.pem ./local/certs/chain.pem
}

setup_certificates() {
  local use_self_signed="$1"
  
  if [ "$use_self_signed" = true ]; then
    generate_self_signed_certificates
  else
    retrieve_ssl_certificates
  fi
}
