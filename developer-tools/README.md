# Developer Tools
The scripts in this folder are intended for special developer tasks occurring only rarely. The scripts present in this
folder are not subject to the same quality standards as the rest of the code. Please keep that in mind when using them.

## fetch_ssl_certs_for_sandbox.sh
Run this script on your own machine (outside the opencode docker sandbox) to fetch the local-dev SSL certificate/key
files from letsencrypt.dataland.com via your local SSH agent, and copy them into the running "dataland" sandbox
(using `sbx cp`) at `./local/certs`. This avoids ever placing an SSH private key inside the sandbox.
