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

## Using Azure OpenAI (proxy finder)
By default the proxy finder uses the mocked provider (`PROXY_FINDER_PROVIDER=mock`), so no LLM calls happen. To use the
real nonprod Azure OpenAI deployment:

**Locally:** export `PROXY_FINDER_PROVIDER=azure` and `AZURE_OPENAI_API_KEY` in your shell before starting the stack.
`environments/.env.dev` keeps values that are already set in the shell and contains the nonprod endpoint, deployment
name and API version. The key must never be committed.

**In the opencode sandbox:**
1. Make sure the sandbox may reach `*.openai.azure.com` over HTTPS (allow it in the sbx network policy on the host).
   If the Azure deployment restricts source IPs, ask for your egress IP to be allowlisted.
2. On the host, run `AZURE_OPENAI_API_KEY=<key> ./developer-tools/injectAzureOpenAiKeyForSandbox.sh` (or run it without
   the variable to be prompted). The key is copied into the running sandbox at `~/.azure-openai.env` (mode 600) and
   sourced by `~/.bashrc`; it is not stored in the image or the repository.
3. Open a new shell in the sandbox and start the stack.

## Using Mailjet

**Locally:** export `MAILJET_API_ID` and `MAILJET_API_SECRET` in your shell before starting the stack.
`environments/.env.dev` keeps values that are already set in the shell and contains the nonprod endpoint. The keys must never be committed.

**In the opencode sandbox:**
1. Make sure the sandbox may reach `api.mailjet.com` over HTTPS (allow it in the sbx network policy on the host).
2. On the host, run `MAILJET_API_ID=<id> MAILJET_API_SECRET=<secret> ./developer-tools/injectAzureOpenAiKeyForSandbox.sh` (or run it without
   the variable to be prompted). The keys are copied into the running sandbox at `~/.mailjet.env` (mode 600) and
   sourced by `~/.bashrc`; they are not stored in the image or the repository.
3. Open a new shell in the sandbox and start the stack.
