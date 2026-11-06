#!/usr/bin/env bash
set -euo pipefail

# Run on the host machine (outside the sandbox). Copies the Azure OpenAI API key of the nonprod deployment into the
# running sandbox at runtime. The key is never baked into the image and never written into the repository.
# Usage: AZURE_OPENAI_API_KEY=... ./injectAzureOpenAiKeyForSandbox.sh   (or omit the variable to be prompted)

sandbox_name="dataland"
sandbox_env_file="/home/agent/.azure-openai.env"
bashrc_hook="[ -f $sandbox_env_file ] && { set -a; source $sandbox_env_file; set +a; }"

if ! command -v sbx >/dev/null 2>&1; then
  echo "sbx is required on the host machine to copy files into the sandbox." >&2
  exit 1
fi

if [[ -z "${AZURE_OPENAI_API_KEY:-}" ]]; then
  read -r -s -p "Azure OpenAI API key (nonprod): " AZURE_OPENAI_API_KEY
  echo
fi
if [[ -z "$AZURE_OPENAI_API_KEY" ]]; then
  echo "No API key given." >&2
  exit 1
fi

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
chmod 700 "$tmp_dir"
(umask 077 && {
  echo "COMPANY_SERVICE_PROVIDER=azure"
  echo "AZURE_OPENAI_API_KEY=$AZURE_OPENAI_API_KEY"
} >"$tmp_dir/azure-openai.env")

echo "Copying the key into sandbox '$sandbox_name' at $sandbox_env_file..."
sbx cp "$tmp_dir/azure-openai.env" "$sandbox_name:$sandbox_env_file"
sbx exec "$sandbox_name" sudo chown agent:agent "$sandbox_env_file"
sbx exec "$sandbox_name" chmod 600 "$sandbox_env_file"
# Sourced after .env.dev in ~/.bashrc, so the values are present in every new shell (before starting the stack).
sbx exec -u agent "$sandbox_name" bash -c "grep -qF '$sandbox_env_file' ~/.bashrc || echo '$bashrc_hook' >> ~/.bashrc"

echo "Done. Open a new shell in the sandbox and (re)start the stack to use the Azure OpenAI nonprod deployment."
