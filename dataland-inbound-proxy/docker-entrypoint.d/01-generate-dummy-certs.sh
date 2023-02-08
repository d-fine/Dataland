#!/bin/bash
# Ensure /certs director is present and permissions are configured accordingly
echo Generating Dummy Certificates to allow nginx boot
mkdir -p /certs
chown -R root:root /certs
chmod -R 600 /certs

# Generate DH params (if not present)
if [ ! -f "/certs/dhparam.pem" ]; then
  openssl dhparam -out /certs/dhparam.pem 2048
fi


# Generate dummy certificates (if not present)
if [ ! -d "/certs/dummy" ]; then
  echo "Creating Dummy certificates for use if LetsEncrypt certs are not (yet) available"
  mkdir -p /certs/dummy
  openssl req -x509 -nodes -newkey rsa:2048 -days 36500 \
      -keyout "/certs/dummy/privkey.pem" \
      -out "/certs/dummy/fullchain.pem" \
      -subj "/CN=Dummy Certificate"
fi

# Note: Calling this script DOES NOT request LetsEncrypt certs if they are not present
# This is because this script is called before NGINX starts and certificates can only be requested after NGINX has started.
# The initial certificate request will be initiated in the deployment script to prevent potential looping LetsEncrypt request because of some error
sh /scripts/configure-certificate-link.sh
