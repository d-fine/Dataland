const cookiebotScriptId = 'Cookiebot';
const cookiebotScriptSrc = 'https://consent.cookiebot.com/uc.js';
const cookiebotCbid = 'cba5002e-6f0e-4848-aadc-ccc8d5c96c86';

/**
 * Loads the Cookiebot consent script unless it has been explicitly disabled via the
 * VITE_COOKIEBOT_ENABLED environment variable (used to suppress the cookie banner in
 * e2e test environments, where it would otherwise cover the UI and break tests).
 */
export function loadCookiebotIfEnabled(): void {
  const isCookiebotExplicitlyDisabled = import.meta.env.VITE_COOKIEBOT_ENABLED === 'false';
  if (isCookiebotExplicitlyDisabled || document.getElementById(cookiebotScriptId)) {
    return;
  }

  const cookiebotScript = document.createElement('script');
  cookiebotScript.id = cookiebotScriptId;
  cookiebotScript.src = cookiebotScriptSrc;
  cookiebotScript.type = 'text/javascript';
  cookiebotScript.async = true;
  cookiebotScript.dataset.cbid = cookiebotCbid;
  document.head.appendChild(cookiebotScript);
}
