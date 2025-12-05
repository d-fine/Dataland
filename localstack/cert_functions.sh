#!/usr/bin/env bash

retrieve_ssl_certificates() {
  mkdir -p ./local/certs
  scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs
}

generate_self_signed_certificates() {
  mkdir -p ./local/certs
  
  if [ -f ./local/certs/privkey.pem ] && [ -f ./local/certs/fullchain.pem ]; then
    echo "Self-signed SSL certificates already exist. Skipping generation."
    return 0
  fi
  
  echo "Generating self-signed SSL certificates..."
  MSYS_NO_PATHCONV=1 openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
    -keyout ./local/certs/privkey.pem \
    -out ./local/certs/fullchain.pem \
    -subj "/C=DE/ST=Hessen/L=Frankfurt/O=DatalandTest/CN=local-dev.dataland.com"
  cp ./local/certs/fullchain.pem ./local/certs/cert.pem
  cp ./local/certs/fullchain.pem ./local/certs/chain.pem
  echo "Self-signed certificates generated successfully"
}

setup_certificates() {
  local use_self_signed="$1"
  
  if [ "$use_self_signed" = true ]; then
    generate_self_signed_certificates
  else
    retrieve_ssl_certificates
  fi
}
