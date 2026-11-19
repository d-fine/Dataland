# Developer Tools
The scripts in this folder are intended for special developer tasks occurring only rarely. The scripts present in this
folder are not subject to the same quality standards as the rest of the code. Please keep that in mind when using them.

## fetch_ssl_certs_for_sandbox.sh
Run this script on your own machine (outside the opencode docker sandbox) to fetch the local-dev SSL certificate/key
files from letsencrypt.dataland.com via your local SSH agent, and copy them into the running "dataland" sandbox
(using `sbx cp`) at `./local/certs`. This avoids ever placing an SSH private key inside the sandbox.

## ensureCypressGuiForwarding.sh
Idempotently starts Xvfb, x11vnc, and noVNC (websockify) inside the sandbox so the Cypress GUI (e.g. `npm run
cypress` in `dataland-frontend`) can be viewed from the desktop at `http://localhost:6080/vnc.html`. It is installed
and started once by `initialSetup.sh`, which also hooks it into `~/.bashrc` so it self-heals on every new shell,
surviving sandbox restarts. Safe to re-run manually at any time.

## Using Azure OpenAI (proxy finder) and Mailjet
By default the proxy finder uses the mocked provider (`COMPANY_SERVICE_PROVIDER=mock`) and Mailjet uses placeholder
credentials. To use the real nonprod services, set these variables in your shell (or sandbox environment) before
starting the stack: `COMPANY_SERVICE_PROVIDER=azure`, `AZURE_OPENAI_API_KEY`, `AZURE_OPENAI_ENDPOINT` (must end with
`/openai/v1`), `AZURE_OPENAI_DEPLOYMENT_NAME`, `MAILJET_API_ID` and `MAILJET_API_SECRET`.

`environments/.env.dev` uses the `${VAR:-default}` syntax, so values that are already set in the environment are kept.
Secrets must never be committed. In the opencode sandbox, make sure it may reach `*.openai.azure.com` and
`api.mailjet.com` over HTTPS (sbx network policy on the host).
