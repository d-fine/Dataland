declare global {
  interface Window {
    Cookiebot?: {
      renew: () => void;
    };
  }
  var Cookiebot: Window['Cookiebot'];
}

export function initCookiebot(): void {
  document.querySelectorAll<HTMLButtonElement>('[data-cookiebot-renew]').forEach((btn) => {
    btn.addEventListener('click', () => {
      globalThis.Cookiebot?.renew();
    });
  });
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCookiebot);
  } else {
    initCookiebot();
  }
}
