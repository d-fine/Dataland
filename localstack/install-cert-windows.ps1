<#
.SYNOPSIS
  Imports the self-signed local dev certificate for local-dev.dataland.com
  into the Windows Trusted Root store (one-time per machine).

.PARAMETER CertPath
  Path to fullchain.pem. Defaults to local/certs/fullchain.pem in the repo.

.NOTES
  Run in an elevated PowerShell. If the site is still blocked afterward,
  clear a cached HSTS policy at edge://net-internals/#hsts.
#>

param(
    [string]$CertPath = (Join-Path $PSScriptRoot "..\local\certs\fullchain.pem")
)

if (-not (Test-Path $CertPath)) {
    Write-Error "Certificate not found at '$CertPath'. Run manageLocalStack.sh --start first, or pass -CertPath explicitly."
    exit 1
}

Write-Host "Importing '$CertPath' into the LocalMachine Trusted Root store..."
certutil -addstore -f "ROOT" "$CertPath"

Write-Host ""
Write-Host "Done. Restart Edge/Chrome for the change to take effect."
